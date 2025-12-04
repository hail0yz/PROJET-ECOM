#!/bin/bash

###############################################
# Script de déploiement automatisé pour Azure
# Usage: ./deploy-to-azure.sh <IP_VM> <SSH_USER>
###############################################

set -e  # Arrêter en cas d'erreur

# Couleurs pour les messages
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Fonction pour afficher les messages
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Vérifier les arguments
if [ $# -ne 2 ]; then
    log_error "Usage: $0 <IP_VM> <SSH_USER>"
    log_info "Example: $0 20.123.45.67 azureuser"
    exit 1
fi

VM_IP=$1
SSH_USER=$2
SSH_CONNECTION="${SSH_USER}@${VM_IP}"
PROJECT_NAME="PROJET-ECOM"
REMOTE_DIR="/home/${SSH_USER}/apps/${PROJECT_NAME}"

log_info "Déploiement vers ${SSH_CONNECTION}"

# Vérifier la connexion SSH
log_info "Vérification de la connexion SSH..."
if ! ssh -o ConnectTimeout=10 -o StrictHostKeyChecking=no ${SSH_CONNECTION} "echo 'SSH OK'" > /dev/null 2>&1; then
    log_error "Impossible de se connecter à ${SSH_CONNECTION}"
    log_info "Assurez-vous que:"
    log_info "  1. La VM est démarrée"
    log_info "  2. Le port SSH (22) est ouvert"
    log_info "  3. Vous avez les bonnes clés SSH configurées"
    exit 1
fi
log_info "Connexion SSH établie ✓"

# 1. Installer les dépendances si nécessaire
log_info "Vérification et installation des dépendances..."
ssh ${SSH_CONNECTION} 'bash -s' << 'ENDSSH'
#!/bin/bash

check_and_install() {
    if ! command -v $1 &> /dev/null; then
        echo "Installation de $1..."
        case $1 in
            docker)
                sudo apt update
                sudo apt install -y apt-transport-https ca-certificates curl software-properties-common
                curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
                echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
                sudo apt update
                sudo apt install -y docker-ce docker-ce-cli containerd.io
                sudo usermod -aG docker $USER
                ;;
            docker-compose)
                sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
                sudo chmod +x /usr/local/bin/docker-compose
                ;;
            git)
                sudo apt update && sudo apt install -y git
                ;;
            java)
                sudo apt update && sudo apt install -y openjdk-17-jdk
                ;;
            mvn)
                sudo apt update && sudo apt install -y maven
                ;;
            node)
                curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
                sudo apt install -y nodejs
                ;;
        esac
    else
        echo "$1 est déjà installé ✓"
    fi
}

# Vérifier et installer les dépendances
check_and_install docker
check_and_install git
check_and_install java
check_and_install mvn
check_and_install node

# Vérifier docker-compose séparément
if ! docker-compose --version &> /dev/null; then
    check_and_install docker-compose
fi

echo "Toutes les dépendances sont installées ✓"
ENDSSH

log_info "Dépendances installées ✓"

# 2. Créer le répertoire et cloner/mettre à jour le projet
log_info "Préparation du projet sur la VM..."
ssh ${SSH_CONNECTION} bash -s ${PROJECT_NAME} ${REMOTE_DIR} << 'ENDSSH'
PROJECT_NAME=$1
REMOTE_DIR=$2

mkdir -p ~/apps

if [ -d "$REMOTE_DIR" ]; then
    echo "Mise à jour du projet existant..."
    cd $REMOTE_DIR
    
    # Sauvegarder les modifications locales
    git stash
    
    # Récupérer les dernières modifications
    git fetch origin
    git checkout feature/deployment
    git pull origin feature/deployment
    
    # Restaurer les modifications locales si nécessaire
    git stash pop || true
else
    echo "Clonage du projet..."
    cd ~/apps
    git clone https://github.com/hail0yz/PROJET-ECOM.git
    cd $PROJECT_NAME
    git checkout feature/deployment
fi

echo "Projet préparé ✓"
ENDSSH

log_info "Projet préparé ✓"

# 3. Transférer les fichiers de configuration nécessaires
log_info "Transfert des fichiers de configuration..."

# Vérifier si .env existe localement
if [ -f ".env" ]; then
    log_info "Transfert du fichier .env..."
    scp .env ${SSH_CONNECTION}:${REMOTE_DIR}/.env
else
    log_warning "Fichier .env non trouvé localement. Création d'un template..."
    ssh ${SSH_CONNECTION} bash -s ${REMOTE_DIR} << 'ENDSSH'
REMOTE_DIR=$1
cat > ${REMOTE_DIR}/.env << 'EOF'
# Stripe API Key
STRIPE_API_KEY=sk_test_your_key_here

# Base de données
DB_PASSWORD=change_this_password

# Keycloak
KEYCLOAK_ADMIN_PASSWORD=change_this_password

# VM Public IP
PUBLIC_IP=your_vm_ip_here
EOF
echo "Template .env créé. Veuillez le configurer avant de démarrer l'application."
ENDSSH
fi

# Transférer docker-compose.prod.yml s'il existe
if [ -f "docker-compose.prod.yml" ]; then
    log_info "Transfert de docker-compose.prod.yml..."
    scp docker-compose.prod.yml ${SSH_CONNECTION}:${REMOTE_DIR}/
else
    log_info "docker-compose.prod.yml sera utilisé depuis le repository"
fi

log_info "Fichiers de configuration transférés ✓"

# 4. Build du backend
log_info "Build du backend (cela peut prendre plusieurs minutes)..."
ssh ${SSH_CONNECTION} bash -s ${REMOTE_DIR} << 'ENDSSH'
REMOTE_DIR=$1
cd $REMOTE_DIR

# Liste des services à builder
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

echo "Building backend services..."
for service in "${services[@]}"; do
    echo "Building $service..."
    cd backend/$service
    
    if mvn clean package -DskipTests; then
        echo "$service built successfully ✓"
    else
        echo "ERROR: Failed to build $service"
        exit 1
    fi
    
    cd $REMOTE_DIR
done

echo "Backend build completed ✓"
ENDSSH

log_info "Backend buildé ✓"

# 5. Build du frontend
log_info "Build du frontend..."
ssh ${SSH_CONNECTION} bash -s ${REMOTE_DIR} << 'ENDSSH'
REMOTE_DIR=$1
cd $REMOTE_DIR/frontend

if [ -f "package.json" ]; then
    echo "Installing frontend dependencies..."
    npm install
    
    echo "Building frontend..."
    npm run build
    
    echo "Frontend build completed ✓"
else
    echo "package.json not found, skipping frontend build"
fi
ENDSSH

log_info "Frontend buildé ✓"

# 6. Démarrer les services avec Docker Compose
log_info "Démarrage des services Docker..."
ssh ${SSH_CONNECTION} bash -s ${REMOTE_DIR} << 'ENDSSH'
REMOTE_DIR=$1
cd $REMOTE_DIR

# Arrêter les conteneurs existants
if [ -f "docker-compose.prod.yml" ]; then
    docker-compose -f docker-compose.prod.yml down || true
elif [ -f "docker-compose.yml" ]; then
    docker-compose down || true
fi

# Démarrer les nouveaux conteneurs
if [ -f "docker-compose.prod.yml" ]; then
    echo "Démarrage avec docker-compose.prod.yml..."
    docker-compose -f docker-compose.prod.yml up -d
else
    echo "Démarrage avec docker-compose.yml..."
    docker-compose up -d
fi

# Attendre que les services démarrent
echo "Attente du démarrage des services (30 secondes)..."
sleep 30

# Vérifier l'état des conteneurs
docker-compose ps

echo "Services Docker démarrés ✓"
ENDSSH

log_info "Services démarrés ✓"

# 7. Vérifier le déploiement
log_info "Vérification du déploiement..."
ssh ${SSH_CONNECTION} bash -s ${REMOTE_DIR} << 'ENDSSH'
REMOTE_DIR=$1
cd $REMOTE_DIR

echo "État des conteneurs:"
docker-compose ps

echo ""
echo "Services en cours d'exécution:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo ""
echo "Logs récents des services:"
docker-compose logs --tail=5

ENDSSH

# 8. Afficher les informations de connexion
log_info "======================================"
log_info "Déploiement terminé avec succès! 🚀"
log_info "======================================"
log_info ""
log_info "Accès aux services:"
log_info "  - Frontend: http://${VM_IP}"
log_info "  - API Gateway: http://${VM_IP}:8080"
log_info "  - Eureka Dashboard: http://${VM_IP}:8761"
log_info "  - Keycloak Admin: http://${VM_IP}:8088/admin"
log_info "  - SonarQube: http://${VM_IP}:9000"
log_info ""
log_info "Pour voir les logs:"
log_info "  ssh ${SSH_CONNECTION} 'cd ${REMOTE_DIR} && docker-compose logs -f'"
log_info ""
log_info "Pour arrêter les services:"
log_info "  ssh ${SSH_CONNECTION} 'cd ${REMOTE_DIR} && docker-compose down'"
log_info ""
log_warning "N'oubliez pas de:"
log_warning "  1. Configurer le fichier .env avec vos vraies clés"
log_warning "  2. Configurer Nginx pour le reverse proxy"
log_warning "  3. Configurer SSL/TLS avec Let's Encrypt"
log_warning "  4. Changer les mots de passe par défaut"
log_info ""
log_info "Consultez AZURE_DEPLOYMENT.md pour plus de détails"

exit 0
