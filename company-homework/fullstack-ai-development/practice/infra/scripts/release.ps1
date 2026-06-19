#!/usr/bin/env pwsh
# ─────────────────────────────────────────────────────────────
#  ATS Release Script · Windows / PowerShell
#  在仓库根目录执行：
#    pwsh -File infra/scripts/release.ps1
#
#  作用：
#   1. 检查 infra/.env.prod 是否存在
#   2. 后端 mvn package（跳测试）
#   3. compose build 全镜像
#   4. compose up -d 启动
#   5. 等待健康检查通过，打印访问地址
# ─────────────────────────────────────────────────────────────

param (
    [switch]$SkipBuild,
    [switch]$SkipTests,
    [string]$ComposeBin = "docker-compose"
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
Set-Location $repoRoot

Write-Host '━━━ ATS Release ━━━' -ForegroundColor Cyan

# 1. env 校验
$envFile = Join-Path $repoRoot 'infra/.env.prod'
if (-not (Test-Path $envFile)) {
    Write-Host "✗ 找不到 $envFile" -ForegroundColor Red
    Write-Host '  请先 cp infra/.env.prod.example infra/.env.prod 并填入实际值' -ForegroundColor Yellow
    exit 1
}
Write-Host "✓ env file present: $envFile"

# 2. JWT keypair 校验（避免 dev key 误带到 prod）
$jwtPriv = Join-Path $repoRoot 'infra/jwt/prod-private.pem'
$jwtPub = Join-Path $repoRoot 'infra/jwt/prod-public.pem'
if (-not (Test-Path $jwtPriv) -or -not (Test-Path $jwtPub)) {
    Write-Host '✗ 缺少 prod-private.pem / prod-public.pem' -ForegroundColor Red
    Write-Host '  生成命令（在 infra/jwt 内）：' -ForegroundColor Yellow
    Write-Host '    openssl genrsa -out prod-private.pem 2048' -ForegroundColor Yellow
    Write-Host '    openssl rsa -in prod-private.pem -pubout -out prod-public.pem' -ForegroundColor Yellow
    exit 1
}
Write-Host '✓ JWT prod keypair present'

# 3. 后端测试（可跳过）
if (-not $SkipTests) {
    Write-Host '━ Running backend tests...' -ForegroundColor Cyan
    bun run be:test
    if ($LASTEXITCODE -ne 0) { Write-Host '✗ 后端测试失败，发布中止' -ForegroundColor Red; exit 1 }
    Write-Host '✓ Backend tests passed'
}

# 4. 镜像构建（可跳过，复用已有 image）
$composeFile = 'infra/docker-compose.prod.yml'
if (-not $SkipBuild) {
    Write-Host '━ Building images...' -ForegroundColor Cyan
    & $ComposeBin -f $composeFile --env-file $envFile build
    if ($LASTEXITCODE -ne 0) { Write-Host '✗ build 失败' -ForegroundColor Red; exit 1 }
    Write-Host '✓ Images built'
}

# 5. 启动（含依赖等待）
Write-Host '━ Starting stack...' -ForegroundColor Cyan
& $ComposeBin -f $composeFile --env-file $envFile up -d
if ($LASTEXITCODE -ne 0) { Write-Host '✗ up -d 失败' -ForegroundColor Red; exit 1 }

# 6. 健康轮询（最多 60s）
Write-Host '━ Waiting for /api/v1/health (max 60s)...' -ForegroundColor Cyan
$webPort = (Get-Content $envFile | Where-Object { $_ -match '^WEB_PORT=' } | ForEach-Object { ($_ -split '=')[1] })
if (-not $webPort) { $webPort = '80' }
$healthUrl = "http://127.0.0.1:$webPort/api/v1/health"
$ok = $false
for ($i = 0; $i -lt 60; $i++) {
    try {
        $resp = Invoke-RestMethod -Uri $healthUrl -TimeoutSec 2 -ErrorAction Stop
        if ($resp.code -eq 0) { $ok = $true; break }
    }
    catch { Start-Sleep -Seconds 1 }
}

if ($ok) {
    Write-Host ''
    Write-Host '━━━ READY ━━━' -ForegroundColor Green
    Write-Host "  Web:    http://127.0.0.1:$webPort/"
    Write-Host "  API:    http://127.0.0.1:$webPort/api/v1/health"
    Write-Host '  Logs:   bun run prod:logs'
    Write-Host '  Stop:   bun run prod:down'
}
else {
    Write-Host '⚠ 60 秒内未就绪，请检查：bun run prod:logs' -ForegroundColor Yellow
    exit 2
}
