# =====================================================
# MediBook - Start All 10 Services
# Compatible with PowerShell 5.1+
# =====================================================

$ROOT = "C:\D\MediBook\MediBook"
$ENV_FILE = "$ROOT\.env"
$ENV_BAT  = "$ROOT\setenv.bat"

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "   MediBook Microservices Startup" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

# ── Step 1: Read .env and write a setenv.bat ──
if (-not (Test-Path $ENV_FILE)) {
    Write-Host "ERROR: .env not found at $ENV_FILE" -ForegroundColor Red; exit 1
}

$batLines = @("@echo off")
Get-Content $ENV_FILE | ForEach-Object {
    $line = $_.Trim()
    if ($line -and -not $line.StartsWith('#') -and $line -match '=') {
        $idx = $line.IndexOf('=')
        $key = $line.Substring(0, $idx).Trim()
        $val = $line.Substring($idx + 1).Trim()
        $batLines += "set $key=$val"
    }
}
$batLines | Set-Content -Path $ENV_BAT -Encoding ASCII
Write-Host "setenv.bat created with all env vars." -ForegroundColor Green

# ── Step 2: Helper to open a new cmd window ──
function Start-Svc($label, $dir) {
    Write-Host "  -> Starting $label ..." -ForegroundColor Yellow
    $cmd = "call `"$ENV_BAT`" && cd /d `"$ROOT\$dir`" && mvn spring-boot:run"
    Start-Process "cmd.exe" -ArgumentList "/k `"$cmd`"" -WindowStyle Normal
}

# ── Step 3: Start in order ──
Write-Host "`n[1/10] Eureka Server (8761)" -ForegroundColor Cyan
Start-Svc "Eureka Server" "eureka-server"
Write-Host "Waiting 25s for Eureka to start..." -ForegroundColor Gray
Start-Sleep -Seconds 25

Write-Host "`n[2-9] Business Services" -ForegroundColor Cyan
Start-Svc "Auth Service"         "auth-service";         Start-Sleep 4
Start-Svc "Provider Service"     "provider-service";     Start-Sleep 4
Start-Svc "Schedule Service"     "schedule-service";     Start-Sleep 4
Start-Svc "Appointment Service"  "appointment-service";  Start-Sleep 4
Start-Svc "Review Service"       "review-service";       Start-Sleep 4
Start-Svc "Record Service"       "record-service";       Start-Sleep 4
Start-Svc "Notification Service" "notification-service"; Start-Sleep 4
Start-Svc "Payment Service"      "payment-service";      Start-Sleep 4

Write-Host "`nWaiting 20s before starting API Gateway..." -ForegroundColor Gray
Start-Sleep -Seconds 20

Write-Host "`n[10/10] API Gateway (8080)" -ForegroundColor Cyan
Start-Svc "API Gateway" "api-gateway"

Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host " All 10 service windows launched!" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host " Eureka Dashboard : http://localhost:8761" -ForegroundColor White
Write-Host " API Gateway      : http://localhost:8080" -ForegroundColor White
Write-Host " Frontend         : http://localhost:5173" -ForegroundColor White
Write-Host "`n Services take 2-3 min to fully boot." -ForegroundColor Gray
