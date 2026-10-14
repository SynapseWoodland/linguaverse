<#
.SYNOPSIS
  一键顺序拉起 LinguaVerse 后端 + 前端 dev 服务（仅封装 README/AGENTS.md 中的命令，
  不引入任何新的运行时或端口假设）。
.DESCRIPTION
  - 后端：mvn spring-boot:run（端口 8080，前缀 /api）
  - 前端：npm run dev（端口 5173，/api 代理到 127.0.0.1:8080）
  每个服务在独立窗口中运行，便于观察日志；直接关闭窗口即可停止对应服务。
  前置：Node 24.x、JDK 17、本地 MySQL（库名 linguaverse）。
.EXAMPLE
  powershell -File scripts/start-all.ps1
#>
$ErrorActionPreference = 'Stop'

# 仓库根 = 本脚本所在目录的上一级
$RepoRoot = Split-Path -Parent $PSScriptRoot
$ServerDir = Join-Path $RepoRoot 'server'
$ClientDir = Join-Path $RepoRoot 'client'

function Test-Tool($name) {
  if (-not (Get-Command $name -ErrorAction SilentlyContinue)) {
    Write-Host "[缺失] 找不到命令 '$name'，请先安装并确保在 PATH 中。" -ForegroundColor Red
    return $false
  }
  return $true
}

# 1) 依赖命令存在性检查（失败即中止，不猜测其他运行时）
$ok = $true
foreach ($t in 'mvn', 'node', 'npm') { if (-not (Test-Tool $t)) { $ok = $false } }
if (-not $ok) { exit 1 }

# 2) 后端
Write-Host '[启动] 后端 mvn spring-boot:run (http://localhost:8080/api) ...' -ForegroundColor Cyan
Start-Process -WorkingDirectory $ServerDir -FilePath 'mvn' -ArgumentList 'spring-boot:run' `
  -WindowStyle Normal

# 3) 前端（首次无 node_modules 时先装依赖）
if (-not (Test-Path (Join-Path $ClientDir 'node_modules'))) {
  Write-Host '[准备] 前端缺少 node_modules，执行 npm ci ...' -ForegroundColor Yellow
  Push-Location $ClientDir
  npm ci
  Pop-Location
}
Write-Host '[启动] 前端 npm run dev (http://localhost:5173) ...' -ForegroundColor Cyan
Start-Process -WorkingDirectory $ClientDir -FilePath 'npm' -ArgumentList 'run', 'dev' `
  -WindowStyle Normal

Write-Host ''
Write-Host '完成。后端 http://localhost:8080/api ，前端 http://localhost:5173' -ForegroundColor Green
Write-Host '停止：关闭对应服务窗口即可。' -ForegroundColor DarkGray
