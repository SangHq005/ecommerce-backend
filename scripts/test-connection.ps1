# PowerShell script to test frontend-backend connection
# Usage: ./scripts/test-connection.ps1

$ErrorActionPreference = "Stop"

Write-Host "Testing Frontend-Backend Connection..." -ForegroundColor Cyan
Write-Host "=======================================" -ForegroundColor Cyan

# Test Backend Health
Write-Host "`n1. Testing Backend Health..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/health" -Method GET
    if ($health.status -eq "UP") {
        Write-Host "✓ Backend is healthy" -ForegroundColor Green
        Write-Host "  Components:" -ForegroundColor Gray
        foreach ($component in $health.components.PSObject.Properties) {
            Write-Host "    - $($component.Name): $($component.Value.status)" -ForegroundColor Gray
        }
    } else {
        Write-Host "✗ Backend health check failed" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ Cannot connect to backend at http://localhost:8080" -ForegroundColor Red
    Write-Host "  Error: $_" -ForegroundColor Red
    exit 1
}

# Test Swagger UI
Write-Host "`n2. Testing Swagger UI..." -ForegroundColor Yellow
try {
    $swagger = Invoke-WebRequest -Uri "http://localhost:8080/swagger-ui/index.html" -Method GET
    if ($swagger.StatusCode -eq 200) {
        Write-Host "✓ Swagger UI is accessible" -ForegroundColor Green
    }
} catch {
    Write-Host "✗ Swagger UI not accessible" -ForegroundColor Red
}

# Test API Docs
Write-Host "`n3. Testing API Documentation..." -ForegroundColor Yellow
try {
    $apiDocs = Invoke-RestMethod -Uri "http://localhost:8080/v3/api-docs" -Method GET
    Write-Host "✓ API documentation available" -ForegroundColor Green
    Write-Host "  API Version: $($apiDocs.info.version)" -ForegroundColor Gray
    Write-Host "  Total Endpoints: $($apiDocs.paths.PSObject.Properties.Count)" -ForegroundColor Gray
} catch {
    Write-Host "✗ API documentation not accessible" -ForegroundColor Red
}

# Test Frontend
Write-Host "`n4. Testing Frontend..." -ForegroundColor Yellow
try {
    $frontend = Invoke-WebRequest -Uri "http://localhost:3000" -Method GET
    if ($frontend.StatusCode -eq 200) {
        Write-Host "✓ Frontend is running" -ForegroundColor Green
    }
} catch {
    Write-Host "✗ Frontend not accessible at http://localhost:3000" -ForegroundColor Red
    Write-Host "  Make sure to run 'npm install' and 'npm run dev' in Frontend directory" -ForegroundColor Yellow
}

# Test Registration Endpoint
Write-Host "`n5. Testing Registration Endpoint..." -ForegroundColor Yellow
$testEmail = "test$(Get-Random)@example.com"
$testUser = @{
    email = $testEmail
    password = "Test@123456"
    fullName = "Test User"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/register" `
        -Method POST `
        -ContentType "application/json" `
        -Body $testUser

    if ($response.success -and $response.data.accessToken) {
        Write-Host "✓ Registration endpoint working" -ForegroundColor Green
        Write-Host "  Access token received (length: $($response.data.accessToken.Length))" -ForegroundColor Gray
        $accessToken = $response.data.accessToken
    } else {
        Write-Host "✗ Registration failed" -ForegroundColor Red
    }
} catch {
    $errorResponse = $_.ErrorDetails.Message | ConvertFrom-Json -ErrorAction SilentlyContinue
    if ($errorResponse) {
        Write-Host "✗ Registration failed: $($errorResponse.message)" -ForegroundColor Red
        if ($errorResponse.code) {
            Write-Host "  Error Code: $($errorResponse.code)" -ForegroundColor Gray
        }
    } else {
        Write-Host "✗ Registration endpoint error: $_" -ForegroundColor Red
    }
}

# Test Authenticated Endpoint
if ($accessToken) {
    Write-Host "`n6. Testing Authenticated Endpoint..." -ForegroundColor Yellow
    try {
        $headers = @{
            "Authorization" = "Bearer $accessToken"
        }
        $me = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/me" `
            -Method GET `
            -Headers $headers

        if ($me.success) {
            Write-Host "✓ Authentication working" -ForegroundColor Green
            Write-Host "  User ID: $($me.data.userId)" -ForegroundColor Gray
            Write-Host "  Roles: $($me.data.roles -join ', ')" -ForegroundColor Gray
        }
    } catch {
        Write-Host "✗ Authentication failed" -ForegroundColor Red
    }
}

# Test CORS
Write-Host "`n7. Testing CORS Configuration..." -ForegroundColor Yellow
try {
    $corsHeaders = @{
        "Origin" = "http://localhost:3000"
        "Access-Control-Request-Method" = "GET"
    }
    $corsResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/catalog/public/products" `
        -Method OPTIONS `
        -Headers $corsHeaders

    if ($corsResponse.Headers["Access-Control-Allow-Origin"]) {
        Write-Host "✓ CORS is properly configured" -ForegroundColor Green
        Write-Host "  Allowed Origin: $($corsResponse.Headers['Access-Control-Allow-Origin'])" -ForegroundColor Gray
    } else {
        Write-Host "✗ CORS not configured" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ CORS test failed" -ForegroundColor Red
}

# Summary
Write-Host "`n=======================================" -ForegroundColor Cyan
Write-Host "Connection Test Summary" -ForegroundColor Cyan
Write-Host "=======================================" -ForegroundColor Cyan

$issues = @()

# Check for common issues
if (-not (Test-Path "../Meta-Shop-Web/Frontend/.env.local")) {
    $issues += "Frontend .env.local file is missing"
}

if ($issues.Count -gt 0) {
    Write-Host "`nIssues Found:" -ForegroundColor Yellow
    foreach ($issue in $issues) {
        Write-Host "  - $issue" -ForegroundColor Yellow
    }
} else {
    Write-Host "`n✓ All connection tests passed!" -ForegroundColor Green
}

Write-Host "`nUseful Commands:" -ForegroundColor Cyan
Write-Host "  Start both servers: ./scripts/start-dev.ps1" -ForegroundColor Gray
Write-Host "  Backend only: cd ecommerce-backend; ./mvnw spring-boot:run" -ForegroundColor Gray
Write-Host "  Frontend only: cd ../Meta-Shop-Web/Frontend; npm run dev" -ForegroundColor Gray
Write-Host "  View API docs: http://localhost:8080/swagger-ui/index.html" -ForegroundColor Gray