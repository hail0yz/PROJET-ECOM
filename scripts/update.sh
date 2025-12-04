#!/bin/bash

###############################################
# Script de mise à jour de l'application
###############################################

set -e

# Couleurs
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_info "Mise à jour de l'application e-commerce..."

# Sauvegarder la branche actuelle
current_branch=$(git branch --show-current)
log_info "Branche actuelle: $current_branch"

# Arrêter l'application
log_info "Arrêt de l'application..."
./scripts/stop.sh

# Sauvegarder les modifications locales
log_info "Sauvegarde des modifications locales..."
git stash

# Récupérer les dernières modifications
log_info "Récupération des dernières modifications..."
git fetch origin
git pull origin $current_branch

# Restaurer les modifications locales si nécessaire
git stash pop || true

# Rebuild du backend
log_info "Rebuild du backend..."
services=(
    "registry-server"
    "api-gateway"
    "bookService"
    "customer-service"
    "cart-service"
    "order"
    "payment"
    "notification"
)

for service in "${services[@]}"; do
    log_info "Building $service..."
    cd backend/$service
    mvn clean package -DskipTests
    cd ../..
done

# Rebuild du frontend si nécessaire
if [ -d "frontend" ] && [ -f "frontend/package.json" ]; then
    log_info "Rebuild du frontend..."
    cd frontend
    npm install
    npm run build
    cd ..
fi

# Redémarrer l'application
log_info "Redémarrage de l'application..."
./scripts/start.sh

log_info ""
log_info "======================================"
log_info "Mise à jour terminée avec succès! ✓"
log_info "======================================"
log_info ""

exit 0
