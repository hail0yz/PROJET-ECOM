#!/bin/bash

###############################################
# Script d'arrêt de l'application
###############################################

set -e

# Couleurs
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Déterminer quel docker-compose utiliser
COMPOSE_FILE="docker-compose.yml"
if [ -f "docker-compose.prod.yml" ]; then
    COMPOSE_FILE="docker-compose.prod.yml"
    log_info "Utilisation de docker-compose.prod.yml"
else
    log_info "Utilisation de docker-compose.yml"
fi

log_info "Arrêt de l'application e-commerce..."

# Arrêter et supprimer les conteneurs
docker-compose -f $COMPOSE_FILE down

log_info ""
log_info "======================================"
log_info "Application arrêtée avec succès! ✓"
log_info "======================================"
log_info ""
log_info "Les volumes de données sont conservés."
log_info "Pour supprimer également les volumes: docker-compose -f $COMPOSE_FILE down -v"
log_info ""

exit 0
