# MediBook AWS RDS Database Setup Script
# This script creates the required schemas for all microservices on your AWS RDS instance.

$ENDPOINT = "medibook-db.c8jqcac8yt9e.us-east-1.rds.amazonaws.com"
$USER = "admin"

$DATABASES = @(
    "medibook_auth",
    "medibook_provider",
    "medibook_schedule",
    "medibook_appointment",
    "medibook_review",
    "medibook_record",
    "medibook_notification",
    "medibook_payment",
    "sonarqube"
)

Write-Host "Connecting to AWS RDS Endpoint: $ENDPOINT" -ForegroundColor Cyan
Write-Host "You will be prompted for your password for each database creation." -ForegroundColor Yellow

foreach ($db in $DATABASES) {
    Write-Host "Creating database: $db..." -ForegroundColor Green
    mysql -h $ENDPOINT -P 3306 -u $USER -pAbhisingh -e "CREATE DATABASE IF NOT EXISTS $db;"
}

Write-Host "AWS RDS Setup Complete!" -ForegroundColor Green
