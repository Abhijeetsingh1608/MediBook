# =====================================================
# MediBook - Build All Docker Images
# Compatible with PowerShell 5.1+
# =====================================================

$ROOT = "C:\D\MediBook\MediBook"
Set-Location $ROOT

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "   MediBook Docker Image Builder" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

# Check if Docker is running
docker info >$null 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Docker is not running. Please start Docker Desktop first." -ForegroundColor Red
    exit 1
}

$services = @(
    "eureka-server",
    "api-gateway",
    "auth-service",
    "provider-service",
    "schedule-service",
    "appointment-service",
    "review-service",
    "record-service",
    "notification-service",
    "payment-service"
)

foreach ($svc in $services) {
    Write-Host "`n[Building] $svc ..." -ForegroundColor Yellow
    
    # Run the build command from the service directory
    $svcPath = Join-Path $ROOT $svc
    Push-Location $svcPath
    docker build -t "medibook/${svc}:latest" .
    Pop-Location
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[Success] Built medibook/$svc:latest" -ForegroundColor Green
    } else {
        Write-Host "[Error] Failed to build $svc" -ForegroundColor Red
        exit 1
    }
}

Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host " All images built successfully!" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Cyan
