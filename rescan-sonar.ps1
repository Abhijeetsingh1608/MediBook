$ErrorActionPreference = "Stop"

Set-Location $PSScriptRoot

Write-Host "Running Maven verify to regenerate JaCoCo reports..." -ForegroundColor Cyan
mvn verify

Write-Host "Running SonarQube scan against localhost..." -ForegroundColor Cyan
if ($env:SONAR_TOKEN) {
    mvn sonar:sonar "-Dsonar.login=$env:SONAR_TOKEN"
} else {
    Write-Host "SONAR_TOKEN is not set. Set it first to upload a fresh analysis." -ForegroundColor Yellow
    Write-Host '$env:SONAR_TOKEN="your_token_here"' -ForegroundColor Yellow
    mvn sonar:sonar
}

Write-Host "SonarQube rescan complete." -ForegroundColor Green
