#!/bin/bash

###############################################
# Script de monitoring de l'application
###############################################

# Couleurs
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
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

log_section() {
    echo -e "${BLUE}======================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}======================================${NC}"
}

# Déterminer quel docker-compose utiliser
COMPOSE_FILE="docker-compose.yml"
if [ -f "docker-compose.prod.yml" ]; then
    COMPOSE_FILE="docker-compose.prod.yml"
fi

clear

log_section "📊 Monitoring E-Commerce Application"
echo ""

# État des conteneurs
log_section "🐳 État des conteneurs"
docker-compose -f $COMPOSE_FILE ps
echo ""

# Statistiques des ressources
log_section "💻 Utilisation des ressources"
docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}" | grep -E "NAME|ecom|keycloak|sonar|registry|gateway|book|customer|cart|order|payment|notification|frontend|postgres"
echo ""

# Espace disque
log_section "💾 Espace disque"
echo "Espace disque du système:"
df -h / | tail -n 1
echo ""
echo "Utilisation Docker:"
docker system df
echo ""

# Santé des services
log_section "🏥 Santé des services"

check_service() {
    local name=$1
    local url=$2
    
    if curl -s -o /dev/null -w "%{http_code}" --connect-timeout 5 "$url" > /dev/null 2>&1; then
        status=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 5 "$url")
        if [ "$status" = "200" ] || [ "$status" = "302" ]; then
            echo -e "${GREEN}✓${NC} $name: OK (HTTP $status)"
        else
            echo -e "${YELLOW}⚠${NC} $name: HTTP $status"
        fi
    else
        echo -e "${RED}✗${NC} $name: Inaccessible"
    fi
}

check_service "API Gateway" "http://localhost:8080/actuator/health"
check_service "Eureka Server" "http://localhost:8761"
check_service "Keycloak" "http://localhost:8088"
check_service "SonarQube" "http://localhost:9000"
echo ""

# Logs récents d'erreur
log_section "🔍 Erreurs récentes"
docker-compose -f $COMPOSE_FILE logs --tail=50 2>&1 | grep -i "error\|exception\|failed" | tail -n 10 || echo "Aucune erreur récente détectée"
echo ""

# Bases de données
log_section "🗄️ État des bases de données"
databases=(
    "cart-postgres"
    "order-postgres"
    "book-postgres"
    "customer-postgres"
    "payment-postgres"
    "postgres-keycloak"
    "sonarqube-db"
)

for db in "${databases[@]}"; do
    if docker ps | grep -q $db; then
        echo -e "${GREEN}✓${NC} $db: En cours d'exécution"
    else
        echo -e "${RED}✗${NC} $db: Arrêté"
    fi
done
echo ""

# Volumes Docker
log_section "📦 Volumes Docker"
docker volume ls | grep -E "ecom|keycloak|sonar|cart|order|book|customer|payment|postgres"
echo ""

# Résumé
log_section "📈 Résumé"
total_containers=$(docker ps | grep -c -E "ecom|keycloak|sonar|registry|gateway|book|customer|cart|order|payment|notification|frontend|postgres" || echo 0)
running_containers=$(docker ps | grep -E "ecom|keycloak|sonar|registry|gateway|book|customer|cart|order|payment|notification|frontend|postgres" | grep -c "Up" || echo 0)

echo "Conteneurs en cours d'exécution: $running_containers/$total_containers"
echo ""

if [ "$running_containers" = "$total_containers" ]; then
    echo -e "${GREEN}✓ Tous les services sont opérationnels${NC}"
else
    echo -e "${YELLOW}⚠ Certains services ne sont pas opérationnels${NC}"
fi

echo ""
log_info "Rafraîchir: ./scripts/monitor.sh"
log_info "Logs: ./scripts/logs.sh [service]"
log_info "Redémarrer: ./scripts/restart.sh"
echo ""

exit 0
