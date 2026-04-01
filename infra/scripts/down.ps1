$ErrorActionPreference = "Stop"

Write-Host "Derrubando infraestrutura Central-LJ..."
docker compose -f (Join-Path $PSScriptRoot "..\\docker-compose.yml") down

