# PowerShell script to extract API endpoints from Java controllers
$controllerDir = "d:\DemoApp\ecommerce-backend\src\main\java\com/example/ecommerce/ecommerce_backend/api/controller"
$outputFile = "d:\DemoApp\ecommerce-backend\target\extracted_apis.json"

if (-not (Test-Path $controllerDir)) {
    Write-Host "Controller directory not found: $controllerDir" -ForegroundColor Red
    Exit
}

$controllers = Get-ChildItem -Path $controllerDir -Filter "*.java"
$apis = @()

foreach ($file in $controllers) {
    $content = Get-Content -Path $file.FullName
    $controllerName = $file.BaseName
    
    # Try to find class-level RequestMapping
    $classMapping = ""
    foreach ($line in $content) {
        if ($line -match '@RequestMapping\(\s*"([^"]+)"\s*\)') {
            $classMapping = $Matches[1]
            break
        }
        if ($line -match '@RequestMapping\(\s*value\s*=\s*"([^"]+)"\s*\)') {
            $classMapping = $Matches[1]
            break
        }
    }
    
    # Process methods
    $methodMapping = ""
    $httpMethod = ""
    
    for ($i = 0; $i -lt $content.Count; $i++) {
        $line = $content[$i]
        
        # Detect mapping annotations
        if ($line -match '@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping|RequestMapping)\(\s*"([^"]*)"\s*\)') {
            $httpMethod = $Matches[1].Replace("Mapping", "").ToUpper()
            if ($httpMethod -eq "REQUEST") { $httpMethod = "ALL" }
            $methodMapping = $Matches[2]
        }
        elseif ($line -match '@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping|RequestMapping)\s*\(') {
            $httpMethod = $Matches[1].Replace("Mapping", "").ToUpper()
            if ($httpMethod -eq "REQUEST") { $httpMethod = "ALL" }
            $methodMapping = ""
        }
        elseif ($line -match '@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping|RequestMapping)\s*$') {
            $httpMethod = $Matches[1].Replace("Mapping", "").ToUpper()
            if ($httpMethod -eq "REQUEST") { $httpMethod = "ALL" }
            $methodMapping = ""
        }
        
        # Detect Java method signature following the mapping
        if ($httpMethod -ne "" -and $line -match 'public\s+([^\s]+)\s+([a-zA-Z0-9_]+)\s*\(') {
            $returnType = $Matches[1]
            $methodName = $Matches[2]
            
            # Look for request body DTO
            $requestDto = "None"
            # Extract parameters
            $paramLine = $line
            $depth = 0
            # If line has unclosed parenthesis, grab next lines
            while ($paramLine -match '\(' -and $paramLine -notmatch '\)' -and $i -lt $content.Count - 1) {
                $i++
                $paramLine += " " + $content[$i]
            }
            if ($paramLine -match '@RequestBody\s+([a-zA-Z0-9_<>\?, ]+)\s+[a-zA-Z0-9_]+') {
                $requestDto = $Matches[1]
            }
            
            $fullEndpoint = $classMapping + $methodMapping
            $fullEndpoint = $fullEndpoint.Replace("//", "/")
            if (-not $fullEndpoint.StartsWith("/")) {
                $fullEndpoint = "/" + $fullEndpoint
            }
            
            $apis += [PSCustomObject]@{
                Controller  = $controllerName
                Endpoint    = $fullEndpoint
                Method      = $httpMethod
                JavaMethod  = $methodName
                RequestDTO  = $requestDto
                ReturnType  = $returnType
            }
            
            $httpMethod = ""
            $methodMapping = ""
        }
    }
}

$apis | ConvertTo-Json -Depth 5 | Out-File -FilePath $outputFile -Encoding utf8
Write-Host "Extracted $($apis.Count) APIs to $outputFile" -ForegroundColor Green
