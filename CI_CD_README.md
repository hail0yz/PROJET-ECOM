# Déploiement automatisé avec GitHub Actions

## 🚀 Vue d'ensemble

Le projet est configuré pour un déploiement automatique sur Azure VM via GitHub Actions. Lorsque vous pushez sur la branche `release`, le workflow :

1. ✅ **Build** tous les services backend avec Maven
2. ✅ **Build** le frontend Angular
3. ✅ **Construit** les images Docker
4. ✅ **Publie** les images sur GitHub Container Registry
5. ✅ **Déploie** automatiquement sur Azure VM
6. ✅ **Vérifie** que tout fonctionne

## 📋 Configuration requise

### 1. Secrets GitHub à configurer

Allez dans **Settings** > **Secrets and variables** > **Actions** :

| Secret | Description | Exemple |
|--------|-------------|---------|
| `AZURE_VM_HOST` | IP publique de votre VM | `20.123.45.67` |
| `AZURE_VM_USER` | Utilisateur SSH | `azureuser` |
| `AZURE_VM_SSH_KEY` | Clé SSH privée | Contenu de `~/.ssh/id_rsa` |
| `STRIPE_API_KEY` | Clé API Stripe | `sk_test_...` |
| `DB_PASSWORD` | Mot de passe BDD | `SecureP@ss123` |
| `KEYCLOAK_ADMIN_PASSWORD` | Mot de passe Keycloak | `Admin@123` |
| `KEYCLOAK_DB_PASSWORD` | Mot de passe BDD Keycloak | `SecureDB@123` |
| `SONAR_DB_PASSWORD` | Mot de passe BDD SonarQube | `SonarDB@123` |

### 2. Préparer la VM Azure

```bash
# Connexion SSH
ssh azureuser@<IP_VM>

# Installer Docker et Docker Compose
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# Installer Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Créer le répertoire de déploiement
mkdir -p ~/ecom-app

# Se déconnecter et reconnecter pour appliquer les permissions Docker
exit
```

### 3. Ouvrir les ports dans Azure

Dans le **Network Security Group** de votre VM :
- Port 22 (SSH)
- Port 80 (HTTP)
- Port 443 (HTTPS)
- Port 8080 (API Gateway)
- Port 8088 (Keycloak)
- Port 8761 (Eureka)
- Port 9000 (SonarQube)

## 🔄 Workflow de déploiement

### Déploiement automatique

```bash
# 1. Faire vos modifications
git checkout -b feature/ma-fonctionnalite
# ... modifications ...
git commit -am "Ajout fonctionnalité"
git push origin feature/ma-fonctionnalite

# 2. Merger sur release (déclenche le déploiement automatique)
git checkout release
git merge feature/ma-fonctionnalite
git push origin release

# 🎉 Le déploiement démarre automatiquement !
```

### Déploiement manuel

Via l'interface GitHub :
1. Allez dans **Actions**
2. Sélectionnez "Build and Deploy to Azure VM"
3. Cliquez sur "Run workflow"
4. Sélectionnez la branche `release`
5. Cliquez sur "Run workflow"

## 📊 Monitoring du déploiement

### Via GitHub Actions

1. Allez dans l'onglet **Actions**
2. Sélectionnez le workflow en cours
3. Suivez les logs en temps réel

### Via la VM Azure

```bash
# Connexion SSH
ssh azureuser@<IP_VM>

# Voir l'état des conteneurs
cd ~/ecom-app
docker-compose -f docker-compose.prod.yml ps

# Voir les logs
docker-compose -f docker-compose.prod.yml logs -f

# Monitoring complet
./scripts/monitor.sh
```

## 🔍 Vérification du déploiement

Après un déploiement réussi, testez les URLs :

- ✅ Frontend: http://\<IP_VM\>
- ✅ API Gateway: http://\<IP_VM\>:8080/actuator/health
- ✅ Eureka: http://\<IP_VM\>:8761
- ✅ Keycloak: http://\<IP_VM\>:8088/admin
- ✅ SonarQube: http://\<IP_VM\>:9000

## 🐳 Images Docker

Les images sont publiées sur GitHub Container Registry :

```
ghcr.io/hail0yz/registry-server:latest
ghcr.io/hail0yz/api-gateway:latest
ghcr.io/hail0yz/book-service:latest
ghcr.io/hail0yz/customer-service:latest
ghcr.io/hail0yz/cart-service:latest
ghcr.io/hail0yz/order-service:latest
ghcr.io/hail0yz/payment-service:latest
ghcr.io/hail0yz/notification-service:latest
ghcr.io/hail0yz/frontend:latest
```

## 🔧 Dépannage

### Le workflow échoue au build

```bash
# Vérifiez que les Dockerfiles existent
ls -la backend/*/Dockerfile
ls -la payment/Dockerfile

# Testez le build localement
cd backend/registry-server
mvn clean package -DskipTests
```

### Le déploiement échoue sur la VM

```bash
# Connexion SSH
ssh azureuser@<IP_VM>

# Vérifier Docker
docker --version
docker-compose --version

# Vérifier les logs
cd ~/ecom-app
docker-compose -f docker-compose.prod.yml logs
```

### Les images ne se pullent pas

```bash
# Sur la VM, se connecter à GHCR
docker login ghcr.io -u hail0yz

# Vérifier que les images existent
docker pull ghcr.io/hail0yz/api-gateway:latest
```

## 📈 Améliorations futures

- [ ] Tests automatisés avant déploiement
- [ ] Blue-green deployment
- [ ] Notifications Slack/Discord
- [ ] Monitoring avec Prometheus/Grafana
- [ ] Backup automatique avant déploiement
- [ ] Rollback automatique en cas d'échec

## 📚 Documentation complète

- **GITHUB_ACTIONS_SETUP.md** - Configuration détaillée des secrets et du workflow
- **AZURE_DEPLOYMENT.md** - Guide complet de déploiement manuel
- **QUICK_START_AZURE.md** - Guide de démarrage rapide

## 🎯 Architecture CI/CD

```
┌─────────────────┐
│   Developer     │
│  git push       │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│         GitHub Actions                  │
│  ┌──────────────────────────────────┐  │
│  │  1. Build Backend (Maven)        │  │
│  │  2. Build Frontend (npm)         │  │
│  │  3. Build Docker Images          │  │
│  │  4. Push to GHCR                 │  │
│  └──────────────────────────────────┘  │
└────────┬────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│     GitHub Container Registry           │
│  📦 registry-server:latest              │
│  📦 api-gateway:latest                  │
│  📦 book-service:latest                 │
│  📦 ...                                 │
└────────┬────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│          Azure VM                       │
│  ┌──────────────────────────────────┐  │
│  │  1. Pull latest images           │  │
│  │  2. docker-compose down          │  │
│  │  3. docker-compose up -d         │  │
│  │  4. Verify deployment            │  │
│  └──────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

---

**🚀 Prêt pour le déploiement continu !**
