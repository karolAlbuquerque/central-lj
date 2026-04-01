$ErrorActionPreference = "Stop"

Write-Host "Subindo infraestrutura Central-LJ (Kafka + Kafka UI + PostgreSQL)..."
docker compose -f (Join-Path $PSScriptRoot "..\\docker-compose.yml") up -d

Write-Host ""
Write-Host "Portas:"
Write-Host "  Kafka:      localhost:9092"
Write-Host "  Kafka UI:   http://localhost:8088"
Write-Host "  PostgreSQL: localhost:5433 -> container:5432 (db=central_lj user=central_lj pass=central_lj)"
