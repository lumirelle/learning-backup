# ─────────────────────────────────────────────────────────────
#  生成 JWT RS256 公私钥对，并输出 base64 字符串
#  用法：pwsh -File scripts/new-jwt-keys.ps1 -OutDir ats-backend/secrets
# ─────────────────────────────────────────────────────────────

param(
    [string]$OutDir = 'ats-backend/secrets'
)

if (-not (Get-Command openssl -ErrorAction SilentlyContinue)) {
    Write-Host '[X] 未找到 openssl，请先安装（Git for Windows 自带 / winget install openssl）' -ForegroundColor Red
    exit 1
}

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

$priv = Join-Path $OutDir 'jwt-private.pem'
$pub = Join-Path $OutDir 'jwt-public.pem'

openssl genrsa -out $priv 2048 2>$null
openssl rsa -in $priv -pubout -out $pub 2>$null

$privB64 = [Convert]::ToBase64String([IO.File]::ReadAllBytes($priv))
$pubB64 = [Convert]::ToBase64String([IO.File]::ReadAllBytes($pub))

[IO.File]::WriteAllText("$priv.b64", $privB64)
[IO.File]::WriteAllText("$pub.b64", $pubB64)

Write-Host ''
Write-Host "[OK] 密钥已生成于：$OutDir" -ForegroundColor Green
Write-Host '    jwt-private.pem / jwt-public.pem / *.b64'
Write-Host ''
Write-Host '将下面两行追加到 ats-backend/.env (单引号包裹防 shell 转义)：' -ForegroundColor Cyan
Write-Host ''
Write-Host "JWT_PRIVATE_KEY_B64='$privB64'"
Write-Host "JWT_PUBLIC_KEY_B64='$pubB64'"
Write-Host ''
Write-Host '[!] secrets/ 已在 .gitignore，禁止 commit / 截图 / 上传日志泄露' -ForegroundColor Yellow
