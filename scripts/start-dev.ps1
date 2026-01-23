# PowerShell script to start both frontend and backend for development
# Usage: ./scripts/start-dev.ps1

Write-Host "Starting E-Commerce Development Environment..." -ForegroundColor Green

# Check if required tools are installed
Write-Host "Checking prerequisites..." -ForegroundColor Yellow

# Check Java
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "✓ Java found: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Java not found. Please install Java 17 or higher" -ForegroundColor Red
    exit 1
}

# Check Node.js
try {
    $nodeVersion = node --version
    Write-Host "✓ Node.js found: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Node.js not found. Please install Node.js 18 or higher" -ForegroundColor Red
    exit 1
}

# Check npm
try {
    $npmVersion = npm --version
    Write-Host "✓ npm found: $npmVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ npm not found. Please install npm" -ForegroundColor Red
    exit 1
}

# Set environment variables for backend
Write-Host "`nSetting environment variables..." -ForegroundColor Yellow
$env:SERVER_PORT = "8080"
$env:SPRING_PROFILES_ACTIVE = "dev"

# Generate JWT secret if not exists
if (-not $env:JWT_SECRET_BASE64) {
    Write-Host "Generating JWT secret..." -ForegroundColor Yellow
    $bytes = New-Object byte[] 32
    [System.Security.Cryptography.RNGCryptoServiceProvider]::new().GetBytes($bytes)
    $env:JWT_SECRET_BASE64 = [System.Convert]::ToBase64String($bytes)
    Write-Host "JWT secret generated (temporary - set JWT_SECRET_BASE64 env var for persistence)" -ForegroundColor Yellow
}

# Start Backend
Write-Host "`nStarting Backend Server..." -ForegroundColor Green
$backend = Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd ecommerce-backend; ./mvnw spring-boot:run" -PassThru

# Wait for backend to start
Write-Host "Waiting for backend to start..." -ForegroundColor Yellow
$maxAttempts = 30
$attempt = 0
$backendReady = $false

while ($attempt -lt $maxAttempts -and -not $backendReady) {
    $attempt++
    Start-Sleep -Seconds 2
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/health" -Method GET -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            $backendReady = $true
            Write-Host "✓ Backend is ready!" -ForegroundColor Green
        }
    } catch {
        Write-Host "." -NoNewline
    }
}

if (-not $backendReady) {
    Write-Host "`n✗ Backend failed to start. Check the logs." -ForegroundColor Red
    exit 1
}

# Start Frontend
Write-Host "`nStarting Frontend Server..." -ForegroundColor Green
$frontend = Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '../Meta-Shop-Web/Frontend'; npm install; npm run dev" -PassThru

# Wait for frontend to start
Write-Host "Waiting for frontend to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

# Display URLs
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Development Environment Started!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Frontend URL:    http://localhost:3000" -ForegroundColor Yellow
Write-Host "Backend URL:     http://localhost:8080" -ForegroundColor Yellow
Write-Host "Swagger UI:      http://localhost:8080/swagger-ui/index.html" -ForegroundColor Yellow
Write-Host "Health Check:    http://localhost:8080/health" -ForegroundColor Yellow
Write-Host ""
Write-Host "Press Ctrl+C to stop all services" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Keep script running
try {
    while ($true) {
        Start-Sleep -Seconds 1
    }
} finally {
    Write-Host "`nStopping services..." -ForegroundColor Yellow
    if ($backend) { Stop-Process -Id $backend.Id -Force -ErrorAction SilentlyContinue }
    if ($frontend) { Stop-Process -Id $frontend.Id -Force -ErrorAction SilentlyContinue }
    Write-Host "Services stopped." -ForegroundColor Green
}