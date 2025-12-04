#!/bin/bash

###############################################
# Script de démarrage de l'application
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

# Vérifier que Docker est en cours d'exécution
if ! docker info > /dev/null 2>&1; then
    log_error "Docker n'est pas en cours d'exécution"
    exit 1
fi

# Déterminer quel docker-compose utiliser
COMPOSE_FILE="docker-compose.yml"
if [ -f "docker-compose.prod.yml" ]; then
    COMPOSE_FILE="docker-compose.prod.yml"
    log_info "Utilisation de docker-compose.prod.yml"
else
    log_info "Utilisation de docker-compose.yml"
fi

# Vérifier si le fichier .env existe
if [ ! -f ".env" ]; then
    log_warning "Fichier .env non trouvé. Création d'un template..."
    cat > .env << 'EOF'
# Stripe API Key
STRIPE_API_KEY=sk_test_your_key_here

# Base de données
DB_PASSWORD=change_this_password

# Keycloak
KEYCLOAK_ADMIN_PASSWORD=change_this_password
KEYCLOAK_DB_PASSWORD=change_this_password

# SonarQube
SONAR_DB_PASSWORD=change_this_password

# VM Public IP
PUBLIC_IP=your_vm_ip_here
EOF
    log_warning "Veuillez configurer le fichier .env avant de continuer"
    exit 1
fi

log_info "Démarrage de l'application e-commerce..."

# Créer les réseaux si nécessaire
log_info "Préparation de l'environnement..."

# Démarrer les services (ne redémarre pas ceux déjà lancés)
log_info "Démarrage des services..."
log_info "Les services déjà actifs ne seront pas redémarrés"
docker-compose -f $COMPOSE_FILE up -d --no-recreate

# Attendre que les services démarrent
log_info "Attente du démarrage des services (60 secondes)..."
sleep 60

# Vérifier l'état des services
log_info "Vérification de l'état des services..."
docker-compose -f $COMPOSE_FILE ps

# Afficher les services en cours d'exécution
log_info ""
log_info "======================================"
log_info "Services démarrés avec succès! 🚀"
log_info "======================================"
log_info ""
log_info "Services disponibles:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "ecom|keycloak|sonar|registry|api-gateway|book|customer|cart|order|payment|notification|frontend"

log_info ""
log_info "URLs d'accès:"
log_info "  - Frontend: http://localhost"
log_info "  - API Gateway: http://localhost:8080"
log_info "  - Eureka Dashboard: http://localhost:8761"
log_info "  - Keycloak Admin: http://localhost:8088/admin"
log_info "  - SonarQube: http://localhost:9000"
log_info ""
log_info "Pour voir les logs: docker-compose -f $COMPOSE_FILE logs -f"
log_info "Pour arrêter: ./scripts/stop.sh"
log_info ""

exit 0
