#!/bin/bash

###############################################
# Script pour afficher les logs
###############################################

# Déterminer quel docker-compose utiliser
COMPOSE_FILE="docker-compose.yml"
if [ -f "docker-compose.prod.yml" ]; then
    COMPOSE_FILE="docker-compose.prod.yml"
fi

# Si un service est spécifié, afficher ses logs
if [ $# -eq 1 ]; then
    echo "Affichage des logs de $1..."
    docker-compose -f $COMPOSE_FILE logs -f --tail=100 $1
else
    echo "Affichage des logs de tous les services..."
    echo "Utilisez Ctrl+C pour arrêter"
    echo ""
    docker-compose -f $COMPOSE_FILE logs -f --tail=50
fi
