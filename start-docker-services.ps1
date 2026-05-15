# =====================================================
# MediBook - Start Docker Services One-by-One
# =====================================================

$ROOT = "C:\D\MediBook\MediBook"
Set-Location $ROOT

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "   MediBook Docker Startup (One-by-One)" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

# 1. Start Infrastructure (DB, Redis, RabbitMQ)
Write-Host "`n[1/4] Starting Infrastructure (MySQL, Redis, RabbitMQ)..." -ForegroundColor Yellow
docker compose up -d mysql redis rabbitmq
Write-Host "Waiting 15s for DBs to be healthy..." -ForegroundColor Gray
Start-Sleep -Seconds 15

# 2. Start Eureka Server
Write-Host "`n[2/4] Starting Eureka Server..." -ForegroundColor Yellow
docker compose up -d eureka-server
Write-Host "Waiting 20s for Eureka to initialize..." -ForegroundColor Gray
Start-Sleep -Seconds 20

# 3. Start SonarQube
Write-Host "`n[3/4] Starting SonarQube..." -ForegroundColor Yellow
docker compose up -d sonarqube
Write-Host "SonarQube is starting in background..." -ForegroundColor Gray

# 4. Start Microservices one by one
$services = @(
    "auth-service",
    "provider-service",
    "schedule-service",
    "appointment-service",
    "review-service",
    "record-service",
    "notification-service",
    "payment-service",
    "api-gateway"
)

Write-Host "`n[4/4] Starting 9 Microservices..." -ForegroundColor Yellow
foreach ($svc in $services) {
    Write-Host "  -> Starting $svc ..." -ForegroundColor Cyan
    docker compose up -d $svc
    Start-Sleep -Seconds 5
}

Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host " All containers are launching!" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host " Eureka Dashboard : http://localhost:8761" -ForegroundColor White
Write-Host " API Gateway      : http://localhost:8080" -ForegroundColor White
Write-Host " SonarQube        : http://localhost:9000" -ForegroundColor White
Write-Host "`n Use 'docker compose ps' to check status." -ForegroundColor Gray
