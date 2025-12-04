# Guide de Déploiement sur Azure VM

Ce guide vous accompagne étape par étape pour déployer votre application e-commerce sur une machine virtuelle Azure.

## Prérequis

- Un compte Azure actif
- Azure CLI installé localement (optionnel mais recommandé)
- Accès SSH à votre VM
- Domaine ou IP publique pour votre VM

## Étape 1: Créer une VM Azure

### Via le portail Azure

1. Connectez-vous au [portail Azure](https://portal.azure.com)
2. Cliquez sur "Créer une ressource" > "Machine virtuelle"
3. Configurez les paramètres :
   - **Système d'exploitation** : Ubuntu 22.04 LTS (recommandé)
   - **Taille** : Standard_D4s_v3 (4 vCPU, 16 GiB RAM) minimum
   - **Authentification** : Clé SSH (générez une nouvelle paire ou utilisez une existante)
   - **Ports entrants** : SSH (22), HTTP (80), HTTPS (443), 8080 (API Gateway), 8761 (Eureka), 8088 (Keycloak)

### Via Azure CLI

```bash
# Créer un groupe de ressources
az group create --name ecom-rg --location westeurope

# Créer la VM
az vm create \
  --resource-group ecom-rg \
  --name ecom-vm \
  --image Ubuntu2204 \
  --size Standard_D4s_v3 \
  --admin-username azureuser \
  --generate-ssh-keys \
  --public-ip-sku Standard

# Ouvrir les ports nécessaires
az vm open-port --port 80 --resource-group ecom-rg --name ecom-vm --priority 1001
az vm open-port --port 443 --resource-group ecom-rg --name ecom-vm --priority 1002
az vm open-port --port 8080 --resource-group ecom-rg --name ecom-vm --priority 1003
az vm open-port --port 8088 --resource-group ecom-rg --name ecom-vm --priority 1004
az vm open-port --port 8761 --resource-group ecom-rg --name ecom-vm --priority 1005

# Obtenir l'IP publique
az vm show --resource-group ecom-rg --name ecom-vm --show-details --query publicIps -o tsv
```

## Étape 2: Connexion à la VM

```bash
# Remplacez <IP_PUBLIQUE> par l'adresse IP de votre VM
ssh azureuser@<IP_PUBLIQUE>
```

## Étape 3: Installation des dépendances sur la VM

### Mise à jour du système

```bash
sudo apt update && sudo apt upgrade -y
```

### Installation de Docker

```bash
# Installer les prérequis
sudo apt install -y apt-transport-https ca-certificates curl software-properties-common

# Ajouter le dépôt Docker
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Installer Docker
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io

# Ajouter l'utilisateur au groupe docker
sudo usermod -aG docker $USER
newgrp docker

# Vérifier l'installation
docker --version
```

### Installation de Docker Compose

```bash
# Télécharger Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

# Rendre exécutable
sudo chmod +x /usr/local/bin/docker-compose

# Vérifier l'installation
docker-compose --version
```

### Installation de Git

```bash
sudo apt install -y git
git --version
```

### Installation de Java et Maven

```bash
# Installer Java 17
sudo apt install -y openjdk-17-jdk

# Installer Maven
sudo apt install -y maven

# Vérifier les installations
java -version
mvn -version
```

### Installation de Node.js et npm (pour le frontend)

```bash
# Installer Node.js 20.x
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs

# Vérifier les installations
node --version
npm --version
```

### Installation de Nginx (optionnel mais recommandé)

```bash
sudo apt install -y nginx
sudo systemctl enable nginx
sudo systemctl start nginx
```

## Étape 4: Cloner et configurer le projet

```bash
# Créer un répertoire pour l'application
mkdir -p ~/apps
cd ~/apps

# Cloner le repository
git clone https://github.com/hail0yz/PROJET-ECOM.git
cd PROJET-ECOM

# Checkout sur la branche de déploiement
git checkout feature/deployment
```

## Étape 5: Configuration pour la production

### Configurer les variables d'environnement

```bash
# Créer un fichier .env à la racine du projet
nano .env
```

Ajoutez les variables suivantes :

```env
# Stripe API Key pour le paiement
STRIPE_API_KEY=sk_test_votre_cle_ici

# Base de données
DB_PASSWORD=your_secure_password_here

# Keycloak
KEYCLOAK_ADMIN_PASSWORD=your_admin_password_here

# VM Public IP ou Domain
PUBLIC_IP=<VOTRE_IP_PUBLIQUE_OU_DOMAINE>
```

### Adapter Keycloak pour la production

Modifiez le fichier `docker-compose.prod.yml` pour utiliser votre IP publique ou domaine au lieu de `localhost`.

## Étape 6: Build du projet

### Build du Backend

```bash
# Utiliser le script de lancement
chmod +x launchAppli.sh
./launchAppli.sh
```

Ou manuellement pour chaque service :

```bash
cd backend/registry-server
mvn clean package -DskipTests

cd ../api-gateway
mvn clean package -DskipTests

cd ../bookService
mvn clean package -DskipTests

cd ../customer-service
mvn clean package -DskipTests

cd ../cart-service
mvn clean package -DskipTests

cd ../order
mvn clean package -DskipTests

cd ../payment
mvn clean package -DskipTests

cd ../notification
mvn clean package -DskipTests
```

### Build du Frontend

```bash
cd frontend
npm install
npm run build

# Les fichiers de production seront dans dist/
```

## Étape 7: Démarrer l'application avec Docker Compose

```bash
# Retourner à la racine du projet
cd ~/apps/PROJET-ECOM

# Démarrer tous les services
docker-compose -f docker-compose.prod.yml up -d

# Vérifier que tous les conteneurs sont en cours d'exécution
docker-compose ps

# Voir les logs
docker-compose logs -f
```

## Étape 8: Configuration de Nginx (Optionnel mais recommandé)

Nginx agira comme un reverse proxy pour votre application.

```bash
# Créer la configuration Nginx
sudo nano /etc/nginx/sites-available/ecom
```

Copiez la configuration du fichier `nginx.conf` fourni dans le projet.

```bash
# Activer le site
sudo ln -s /etc/nginx/sites-available/ecom /etc/nginx/sites-enabled/

# Désactiver le site par défaut
sudo rm /etc/nginx/sites-enabled/default

# Tester la configuration
sudo nginx -t

# Redémarrer Nginx
sudo systemctl restart nginx
```

## Étape 9: Configuration du pare-feu (UFW)

```bash
# Activer UFW
sudo ufw enable

# Autoriser les ports nécessaires
sudo ufw allow 22/tcp      # SSH
sudo ufw allow 80/tcp      # HTTP
sudo ufw allow 443/tcp     # HTTPS
sudo ufw allow 8080/tcp    # API Gateway
sudo ufw allow 8088/tcp    # Keycloak
sudo ufw allow 8761/tcp    # Eureka

# Vérifier le statut
sudo ufw status
```

## Étape 10: Configuration SSL avec Let's Encrypt (Recommandé pour la production)

Si vous avez un nom de domaine :

```bash
# Installer Certbot
sudo apt install -y certbot python3-certbot-nginx

# Obtenir un certificat SSL
sudo certbot --nginx -d votre-domaine.com -d www.votre-domaine.com

# Le renouvellement automatique est configuré par défaut
# Tester le renouvellement
sudo certbot renew --dry-run
```

## Étape 11: Vérification du déploiement

### Vérifier les services

```bash
# Vérifier tous les conteneurs
docker-compose ps

# Vérifier les logs
docker-compose logs api-gateway
docker-compose logs keycloak
docker-compose logs bookService
```

### Tester les endpoints

```bash
# Tester Eureka
curl http://localhost:8761

# Tester l'API Gateway
curl http://localhost:8080/actuator/health

# Tester Keycloak
curl http://localhost:8088
```

### Accéder aux services

- **Frontend** : http://<IP_PUBLIQUE> ou http://votre-domaine.com
- **API Gateway** : http://<IP_PUBLIQUE>:8080
- **Eureka Dashboard** : http://<IP_PUBLIQUE>:8761
- **Keycloak Admin** : http://<IP_PUBLIQUE>:8088/admin
- **SonarQube** : http://<IP_PUBLIQUE>:9000

## Étape 12: Scripts de gestion

Utilisez les scripts fournis pour gérer votre application :

```bash
# Démarrer l'application
./scripts/start.sh

# Arrêter l'application
./scripts/stop.sh

# Redémarrer l'application
./scripts/restart.sh

# Voir les logs
./scripts/logs.sh

# Mettre à jour l'application
./scripts/update.sh

# Sauvegarder les données
./scripts/backup.sh
```

## Maintenance et Monitoring

### Voir les logs

```bash
# Logs de tous les services
docker-compose logs -f

# Logs d'un service spécifique
docker-compose logs -f api-gateway

# Logs avec limite de lignes
docker-compose logs --tail=100 -f
```

### Surveiller les ressources

```bash
# Voir l'utilisation des ressources par conteneur
docker stats

# Voir l'espace disque
df -h

# Voir la mémoire
free -h
```

### Nettoyer l'espace disque

```bash
# Nettoyer les conteneurs arrêtés
docker container prune -f

# Nettoyer les images non utilisées
docker image prune -a -f

# Nettoyer les volumes non utilisés
docker volume prune -f

# Nettoyage complet
docker system prune -a -f
```

## Mise à jour de l'application

```bash
cd ~/apps/PROJET-ECOM

# Arrêter les services
docker-compose down

# Récupérer les dernières modifications
git pull origin feature/deployment

# Rebuild si nécessaire
./launchAppli.sh

# Redémarrer les services
docker-compose -f docker-compose.prod.yml up -d
```

## Sauvegarde des données

### Sauvegarder les bases de données

```bash
# Sauvegarder toutes les bases PostgreSQL
docker-compose exec cart-postgres pg_dump -U postgres carts_db > backup_cart_$(date +%Y%m%d).sql
docker-compose exec order-postgres pg_dump -U postgres orders_db > backup_order_$(date +%Y%m%d).sql
docker-compose exec book-postgres pg_dump -U postgres books_db > backup_book_$(date +%Y%m%d).sql
docker-compose exec customer-postgres pg_dump -U postgres customers_db > backup_customer_$(date +%Y%m%d).sql
docker-compose exec payment-postgres pg_dump -U postgres payment_db > backup_payment_$(date +%Y%m%d).sql
```

### Restaurer une sauvegarde

```bash
docker-compose exec -T cart-postgres psql -U postgres carts_db < backup_cart_20241204.sql
```

## Dépannage

### Les conteneurs ne démarrent pas

```bash
# Vérifier les logs
docker-compose logs

# Redémarrer un service spécifique
docker-compose restart api-gateway

# Reconstruire un service
docker-compose up -d --build api-gateway
```

### Problèmes de mémoire

```bash
# Augmenter la mémoire Docker
sudo nano /etc/docker/daemon.json
```

Ajoutez :
```json
{
  "memory": "4g",
  "memory-swap": "4g"
}
```

```bash
sudo systemctl restart docker
```

### Problèmes de connexion entre services

Vérifiez que tous les services sont sur le même réseau Docker et que les healthchecks fonctionnent.

## Sécurité

### Bonnes pratiques

1. **Changer les mots de passe par défaut** dans `docker-compose.prod.yml`
2. **Utiliser des secrets** pour les informations sensibles
3. **Configurer SSL/TLS** avec Let's Encrypt
4. **Limiter l'accès SSH** : Utilisez des clés SSH uniquement, désactivez l'authentification par mot de passe
5. **Mettre à jour régulièrement** le système et les conteneurs
6. **Configurer un pare-feu** avec UFW
7. **Surveiller les logs** pour détecter les activités suspectes

### Désactiver l'authentification SSH par mot de passe

```bash
sudo nano /etc/ssh/sshd_config
```

Modifiez :
```
PasswordAuthentication no
```

```bash
sudo systemctl restart sshd
```

## Monitoring avancé (Optionnel)

### Installer Prometheus et Grafana

Ajoutez ces services à votre `docker-compose.prod.yml` pour un monitoring avancé.

## Support

Pour toute question ou problème :
- Consultez les logs : `docker-compose logs -f`
- Vérifiez la documentation des services individuels
- Consultez le README.md du projet

## Checklist de déploiement

- [ ] VM Azure créée avec les bonnes spécifications
- [ ] Ports ouverts dans Azure NSG
- [ ] Docker et Docker Compose installés
- [ ] Git, Java, Maven, Node.js installés
- [ ] Projet cloné depuis GitHub
- [ ] Variables d'environnement configurées
- [ ] Backend buildé (tous les services)
- [ ] Frontend buildé
- [ ] Docker Compose démarré
- [ ] Nginx configuré
- [ ] SSL configuré (si domaine disponible)
- [ ] Pare-feu UFW configuré
- [ ] Tests des endpoints réussis
- [ ] Scripts de gestion testés
- [ ] Sauvegarde configurée

Votre application e-commerce est maintenant déployée sur Azure ! 🚀
