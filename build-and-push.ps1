# ─────────────────────────────────────────────────────────────────────────────
# build-and-push.ps1
# Run this ONCE on your machine to build and push images to Docker Hub.
# Your friend only needs to run kubectl — they never build anything.
#
# Usage:
#   .\build-and-push.ps1 -Username your-dockerhub-username
# ─────────────────────────────────────────────────────────────────────────────

param(
    [Parameter(Mandatory=$true)]
    [string]$Username
)

$ErrorActionPreference = "Stop"

Write-Host "Logging in to Docker Hub..." -ForegroundColor Cyan
docker login

Write-Host "`nBuilding backend image..." -ForegroundColor Cyan
docker build -t "$Username/testbuddy-backend:latest" ./Backend/ai-api-tester

Write-Host "`nBuilding frontend image..." -ForegroundColor Cyan
docker build -t "$Username/testbuddy-frontend:latest" ./Frontend

Write-Host "`nPushing images to Docker Hub..." -ForegroundColor Cyan
docker push "$Username/testbuddy-backend:latest"
docker push "$Username/testbuddy-frontend:latest"

Write-Host "`nDone! Now update k8s/03-backend.yaml and k8s/04-frontend.yaml" -ForegroundColor Green
Write-Host "Replace DOCKER_HUB_USERNAME with: $Username" -ForegroundColor Yellow
