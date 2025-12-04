#!/bin/bash

###############################################
# Script de sauvegarde des données
###############################################

set -e

# Couleurs
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Créer le répertoire de sauvegarde
BACKUP_DIR="./backups/$(date +%Y%m%d_%H%M%S)"
mkdir -p $BACKUP_DIR

log_info "Sauvegarde des données dans $BACKUP_DIR..."

# Déterminer quel docker-compose utiliser
COMPOSE_FILE="docker-compose.yml"
if [ -f "docker-compose.prod.yml" ]; then
    COMPOSE_FILE="docker-compose.prod.yml"
fi

# Sauvegarder les bases de données
databases=(
    "cart-postgres:carts_db"
    "order-postgres:orders_db"
    "book-postgres:books_db"
    "customer-postgres:customers_db"
    "payment-postgres:payment_db"
    "postgres-keycloak:keycloak"
    "sonarqube-db:sonar"
)

for db in "${databases[@]}"; do
    container="${db%:*}"
    dbname="${db#*:}"
    
    # Déterminer l'utilisateur
    if [ "$container" = "postgres-keycloak" ]; then
        user="keycloak"
    elif [ "$container" = "sonarqube-db" ]; then
        user="sonar"
    else
        user="postgres"
    fi
    
    log_info "Sauvegarde de $container/$dbname..."
    
    if docker-compose -f $COMPOSE_FILE ps | grep -q $container; then
        docker-compose -f $COMPOSE_FILE exec -T $container pg_dump -U $user $dbname > "$BACKUP_DIR/${container}_${dbname}.sql"
        log_info "✓ $container/$dbname sauvegardé"
    else
        log_warning "Container $container n'est pas en cours d'exécution, ignoré"
    fi
done

# Sauvegarder les fichiers de configuration
log_info "Sauvegarde des fichiers de configuration..."
cp -r keycloak/realm.json $BACKUP_DIR/ 2>/dev/null || true
cp .env $BACKUP_DIR/ 2>/dev/null || true
cp $COMPOSE_FILE $BACKUP_DIR/

# Créer une archive compressée
log_info "Compression de la sauvegarde..."
tar -czf "${BACKUP_DIR}.tar.gz" -C $(dirname $BACKUP_DIR) $(basename $BACKUP_DIR)
rm -rf $BACKUP_DIR

log_info ""
log_info "======================================"
log_info "Sauvegarde terminée avec succès! ✓"
log_info "======================================"
log_info ""
log_info "Fichier de sauvegarde: ${BACKUP_DIR}.tar.gz"
log_info ""
log_info "Pour restaurer:"
log_info "  1. Extraire: tar -xzf ${BACKUP_DIR}.tar.gz"
log_info "  2. Restaurer chaque base: docker-compose exec -T <container> psql -U <user> <db> < backup.sql"
log_info ""

exit 0
