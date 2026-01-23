# PowerShell script to run the application with environment variables loaded
# Usage: .\run-dev.ps1

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "E-Commerce Backend - Development" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

$envFile = ".env"

# Check if .env file exists
if (-not (Test-Path $envFile)) {
    Write-Host "ERROR: .env file not found!" -ForegroundColor Red
    Write-Host "Please create .env file from .env.example:" -ForegroundColor Yellow
    Write-Host "  cp .env.example .env" -ForegroundColor Yellow
    exit 1
}

Write-Host "Loading environment variables from $envFile..." -ForegroundColor Green

# Load and export environment variables
Get-Content $envFile | ForEach-Object {
    if ($_ -match '^\s*#' -or $_ -match '^\s*$') {
        # Skip comments and empty lines
        return
    }

    if ($_ -match '^([^=]+)=(.*)$') {
        $name = $matches[1].Trim()
        $value = $matches[2].Trim()

        # Set environment variable for current process
        [Environment]::SetEnvironmentVariable($name, $value, "Process")
        Write-Host "  Loaded: $name" -ForegroundColor DarkGray
    }
}

Write-Host ""
Write-Host "Starting Spring Boot application..." -ForegroundColor Green
Write-Host ""

# Run Maven with environment variables
& .\mvnw.cmd spring-boot:run

# Check exit code
if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "Application failed to start!" -ForegroundColor Red
    exit $LASTEXITCODE
}
