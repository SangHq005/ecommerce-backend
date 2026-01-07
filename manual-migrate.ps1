
# Manual Migration Script
# Reads .env for DB credentials and executes all SQL migration files in order

# Load .env variables
$envFile = ".env"
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        if ($_ -match '^([^=]+)=(.*)$') {
            [Environment]::SetEnvironmentVariable($matches[1], $matches[2])
        }
    }
}

$dbHost = $env:DB_HOST
if (!$dbHost) { $dbHost = "localhost" }
$dbPort = $env:DB_PORT
if (!$dbPort) { $dbPort = "3306" }
$dbUser = $env:DB_USERNAME
if (!$dbUser) { $dbUser = "ecommerce" }
$dbPass = $env:DB_PASSWORD
$dbName = $env:DB_NAME
if (!$dbName) { $dbName = "ecommerce" }

Write-Host "Migrating database: $dbName on ${dbHost}:${dbPort} with user $dbUser"

# Get all SQL files sorted by name
$migrationDir = "src\main\resources\db\migration"
$files = Get-ChildItem -Path $migrationDir -Filter "V*.sql" | Sort-Object Name

# Check if mysql is available
if (!(Get-Command "mysql" -ErrorAction SilentlyContinue)) {
    Write-Error "mysql command not found. Please ensure MySQL client is in your PATH."
    exit 1
}

# Create command file
$cmdFile = "temp_migration_cmd.sql"
Set-Content -Path $cmdFile -Value "SELECT 'Starting Migration...';"

foreach ($file in $files) {
    Write-Host "Adding migration: $($file.Name)"
    Add-Content -Path $cmdFile -Value "SELECT 'Migrating $($file.Name)...';"
    Add-Content -Path $cmdFile -Value "source $($file.FullName);"
}

Write-Host "Executing migrations..."
# Execute using mysql
# Note: Using cmd /c to handle redirection if needed, but direct call is better
# We use Get-Content to pipe generated commands to mysql to avoid limitations with 'source' in some environments if paths have spaces (though here they don't seem to)
# Actually 'source' inside mysql requires forward slashes or escaped backslashes on Windows sometimes.
# Safer way: Read content of each file and pipe it? No, source is better for large files.
# Let's try passing the file to generated script.

# Fix paths for MySQL source command (needs forward slashes)
$content = "SET FOREIGN_KEY_CHECKS=0;`n"
foreach ($file in $files) {
    $path = $file.FullName.Replace("\", "/")
    $content += "SELECT 'Migrating $($file.Name)...';`n"
    $content += "source $path;`n"
}
$content += "SET FOREIGN_KEY_CHECKS=1;`n"
Set-Content -Path $cmdFile -Value $content -Encoding UTF8

# Execute
$cmd = "mysql --default-character-set=utf8mb4 -h $dbHost -P $dbPort -u $dbUser -p`"$dbPass`" $dbName -e `"source $cmdFile`""
Invoke-Expression $cmd

if ($LASTEXITCODE -eq 0) {
    Write-Host "Migration completed successfully!"
} else {
    Write-Host "Migration failed."
}

Remove-Item $cmdFile
