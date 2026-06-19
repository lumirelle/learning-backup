# 演示账号 + seed 数据 e2e 验证脚本（一次性 · 验证用，可删）
param()

$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080/api/v1'

Write-Host '=== /stats/public ==='
$pub = Invoke-RestMethod -Uri "$base/stats/public" -TimeoutSec 5
$pub.data | ConvertTo-Json -Compress | Write-Host

Write-Host ''
Write-Host '=== HR login (hr@ats.local / Admin@123) ==='
$hrBody = @{ email='hr@ats.local'; password='Admin@123' } | ConvertTo-Json
$hr = Invoke-RestMethod -Uri "$base/auth/login" -Method POST -ContentType 'application/json' -Body $hrBody -TimeoutSec 5
$hr.data.user | Select-Object id,role,fullName,email | Format-List

Write-Host '=== CANDIDATE login (candidate@ats.local / Admin@123) ==='
$cBody = @{ email='candidate@ats.local'; password='Admin@123' } | ConvertTo-Json
$c = Invoke-RestMethod -Uri "$base/auth/login" -Method POST -ContentType 'application/json' -Body $cBody -TimeoutSec 5
$c.data.user | Select-Object id,role,fullName,email | Format-List

Write-Host '=== ADMIN login (admin@ats.local / Admin@123) ==='
$aBody = @{ email='admin@ats.local'; password='Admin@123' } | ConvertTo-Json
$a = Invoke-RestMethod -Uri "$base/auth/login" -Method POST -ContentType 'application/json' -Body $aBody -TimeoutSec 5
$a.data.user | Select-Object id,role,fullName,email | Format-List

Write-Host '=== /jobs?status=PUBLISHED 总数 ==='
$jobs = Invoke-RestMethod -Uri "$base/jobs?status=PUBLISHED&size=1" -TimeoutSec 5
"PUBLISHED jobs total = $($jobs.data.total)" | Write-Host
