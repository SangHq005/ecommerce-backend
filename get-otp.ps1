# Script to get OTP for testing
# Usage: .\get-otp.ps1 -PhoneNumber "0353227099"

param(
    [Parameter(Mandatory=$true)]
    [string]$PhoneNumber
)

$apiUrl = "http://localhost:8080/api/v1/auth/otp/send"
$body = @{
    phoneNumber = $PhoneNumber
} | ConvertTo-Json

Write-Host "Sending OTP request for: $PhoneNumber" -ForegroundColor Cyan
Write-Host ""

try {
    $response = Invoke-RestMethod -Uri $apiUrl -Method Post -Body $body -ContentType "application/json"
    
    if ($response.success) {
        Write-Host "✓ OTP sent successfully!" -ForegroundColor Green
        Write-Host ""
        Write-Host "Check the backend terminal/console for the OTP code." -ForegroundColor Yellow
        Write-Host "Look for: >>> TERMINAL OTP: XXXXXX for $PhoneNumber" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "Or check the log file: logs\app.log" -ForegroundColor Yellow
    } else {
        Write-Host "✗ Failed to send OTP: $($response.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response: $responseBody" -ForegroundColor Red
    }
}
