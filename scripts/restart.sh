#!/bin/bash

###############################################
# Script de redémarrage de l'application
###############################################

set -e

# Couleurs
GREEN='\033[0;32m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_info "Redémarrage de l'application e-commerce..."

# Arrêter l'application
./scripts/stop.sh

# Attendre un peu
sleep 5

# Démarrer l'application
./scripts/start.sh

exit 0
