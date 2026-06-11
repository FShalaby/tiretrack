param(
    [switch]$SkipBackend,
    [switch]$SkipFrontend
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot

if (-not $SkipBackend) {
    Push-Location $repoRoot
    try {
        Write-Host "Running backend tests..."
        .\mvnw.cmd clean test
    } finally {
        Pop-Location
    }
}

if (-not $SkipFrontend) {
    Push-Location (Join-Path $repoRoot "frontend")
    try {
        Write-Host "Building frontend..."
        npm run build
    } finally {
        Pop-Location
    }
}

Write-Host "Production check completed."
