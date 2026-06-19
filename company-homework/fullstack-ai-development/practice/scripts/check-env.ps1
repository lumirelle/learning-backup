# ─────────────────────────────────────────────────────────────
#  ATS 环境自检 · PowerShell
#  用法：pwsh -File scripts/check-env.ps1
# ─────────────────────────────────────────────────────────────

$ErrorActionPreference = 'Continue'
$results = @()

function Check {
    param([string]$Name, [scriptblock]$Test, [string]$Detail = '')
    try {
        $ok = & $Test
        $status = if ($ok) { 'OK ' } else { 'X  ' }
    }
    catch { $status = 'X  ' }
    $script:results += [pscustomobject]@{
        Status = $status; Item = $Name; Detail = $Detail
    }
}

function Get-CmdVersion([string]$cmd, [string]$arg = '--version') {
    try {
        $out = & $cmd $arg 2>&1 | Select-Object -First 1
        return [string]$out
    }
    catch { return $null }
}

function Test-Port([int]$port) {
    $tcp = [System.Net.Sockets.TcpClient]::new()
    try {
        $iar = $tcp.BeginConnect('127.0.0.1', $port, $null, $null)
        $ok = $iar.AsyncWaitHandle.WaitOne(200)
        if ($ok -and $tcp.Connected) { return $false }
        return $true
    }
    catch { return $true }
    finally { $tcp.Close() }
}

Write-Host ''
Write-Host 'ATS · 环境自检' -ForegroundColor Cyan
Write-Host '----------------------------------------'

Check 'JDK 21+' { (Get-CmdVersion 'java' '-version') -match '"(2[1-9]|[3-9][0-9])' } (Get-CmdVersion 'java' '-version')
Check 'Maven 3.9+' { (Get-CmdVersion 'mvn' '-v') -match 'Apache Maven 3\.(9|1\d)' } (Get-CmdVersion 'mvn' '-v')
Check 'Node 20+' { (Get-CmdVersion 'node' '-v') -match '^v(2\d|[3-9]\d)' } (Get-CmdVersion 'node' '-v')
Check 'Bun 1.3+' { (Get-CmdVersion 'bun' '-v') -match '^1\.(3|[4-9]|\d{2})' } (Get-CmdVersion 'bun' '-v')
Check 'Git' { Get-CmdVersion 'git' '--version' } (Get-CmdVersion 'git' '--version')
Check 'OpenSSL' { Get-CmdVersion 'openssl' 'version' } (Get-CmdVersion 'openssl' 'version')

# 容器：Docker 或 Podman 二选一
$docker = Get-CmdVersion 'docker' '--version'
$podman = Get-CmdVersion 'podman' '--version'
Check '容器引擎 (Docker/Podman)' { $docker -or $podman } ($docker ?? $podman ?? '未找到')

# compose：v1 / v2 / podman 任一
$compose1 = Get-CmdVersion 'docker-compose' '--version'
$compose2 = $null
try { $compose2 = & docker compose version 2>&1 | Select-Object -First 1 } catch {}
$composeP = $null
try { $composeP = & podman compose version 2>&1 | Select-Object -First 1 } catch {}
Check 'Compose (任一)' { $compose1 -or $compose2 -or $composeP } ($compose1 ?? $compose2 ?? $composeP ?? '未找到')

# 端口空闲
foreach ($p in 5432, 6379, 8080, 5173) {
    Check ("端口 $p 空闲") { Test-Port $p } "127.0.0.1:$p"
}

$results | Format-Table -AutoSize

$fails = ($results | Where-Object Status -eq 'X  ').Count
if ($fails -gt 0) {
    Write-Host ''
    Write-Host "[!] $fails 项未通过，请参考 SKILL.md > Phase 3 > 常见问题预案" -ForegroundColor Yellow
    exit 1
}
Write-Host ''
Write-Host '[OK] 全部通过，可进入 M0' -ForegroundColor Green
