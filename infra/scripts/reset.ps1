$ErrorActionPreference = "Stop"

Write-Host "Resetando infraestrutura Central-LJ (derruba e remove volumes)..."
docker compose -f (Join-Path $PSScriptRoot "..\\docker-compose.yml") down -v

