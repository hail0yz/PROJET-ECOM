# Configuration GitHub Actions pour le déploiement sur Azure

Ce document explique comment configurer les secrets GitHub nécessaires pour le déploiement automatique.

## Secrets GitHub à configurer

Allez dans **Settings** > **Secrets and variables** > **Actions** de votre repository GitHub et ajoutez les secrets suivants :

### 1. Secrets Azure VM

#### `AZURE_VM_HOST`
L'adresse IP publique ou le nom de domaine de votre VM Azure.
```
Exemple: 20.123.45.67
ou: ecom.votredomaine.com
```

#### `AZURE_VM_USER`
Le nom d'utilisateur SSH de la VM (généralement `azureuser`).
```
Exemple: azureuser
```

#### `AZURE_VM_SSH_KEY`
La clé SSH privée pour se connecter à la VM.

Pour obtenir cette clé :
```bash
# Sur votre machine locale, si vous avez généré les clés avec Azure CLI
cat ~/.ssh/id_rsa

# Ou la clé spécifique générée pour Azure
cat ~/.ssh/azure_vm_key
```

Copiez TOUT le contenu incluant les lignes `-----BEGIN OPENSSH PRIVATE KEY-----` et `-----END OPENSSH PRIVATE KEY-----`.

### 2. Secrets de l'application

#### `STRIPE_API_KEY`
Votre clé API Stripe pour les paiements.
```
Exemple: sk_test_51Abc...
ou: sk_live_51Abc... (pour production)
```

Obtenez-la sur : https://dashboard.stripe.com/apikeys

#### `DB_PASSWORD`
Mot de passe sécurisé pour toutes les bases de données PostgreSQL.
```
Exemple: MySecureP@ssw0rd2024!
```

#### `KEYCLOAK_ADMIN_PASSWORD`
Mot de passe pour l'administrateur Keycloak.
```
Exemple: SecureKeycl0ak!Admin
```

#### `KEYCLOAK_DB_PASSWORD`
Mot de passe pour la base de données Keycloak.
```
Exemple: Keycl0akDB!P@ss
```

#### `SONAR_DB_PASSWORD`
Mot de passe pour la base de données SonarQube.
```
Exemple: S0narQub3!DB
```

### 3. Secret automatique

#### `GITHUB_TOKEN`
Ce secret est automatiquement fourni par GitHub Actions. **Vous n'avez pas besoin de le configurer manuellement.**

Il est utilisé pour :
- Se connecter au GitHub Container Registry (GHCR)
- Publier les images Docker

## Configuration du workflow

Le workflow GitHub Actions est configuré dans `.github/workflows/deploy.yml`.

### Déclenchement du workflow

Le workflow se déclenche automatiquement quand vous :
1. **Pushez sur la branche `release`**
   ```bash
   git checkout release
   git merge feature/deployment
   git push origin release
   ```

2. **Déclenchez manuellement** depuis l'interface GitHub
   - Allez dans l'onglet **Actions**
   - Sélectionnez le workflow "Build and Deploy to Azure VM"
   - Cliquez sur "Run workflow"

### Étapes du workflow

Le workflow effectue les actions suivantes :

1. **Build and Push** (parallèle pour chaque service)
   - Checkout du code
   - Installation de Java 17 et Maven
   - Build du service avec Maven
   - Construction de l'image Docker
   - Publication sur GitHub Container Registry

2. **Build Frontend**
   - Checkout du code
   - Installation de Node.js 20
   - Build du frontend Angular
   - Construction de l'image Docker Nginx
   - Publication sur GHCR

3. **Deploy**
   - Copie des fichiers de configuration sur la VM
   - Connexion à GitHub Container Registry
   - Pull des dernières images
   - Arrêt des anciens conteneurs
   - Démarrage des nouveaux conteneurs
   - Nettoyage des anciennes images

4. **Verify**
   - Vérification de l'état des services
   - Test des endpoints (API Gateway, Eureka)

5. **Notify**
   - Notification du succès ou de l'échec du déploiement

## Rendre les images Docker publiques (optionnel)

Par défaut, les images publiées sur GHCR sont privées. Pour les rendre publiques :

1. Allez sur https://github.com/hail0yz?tab=packages
2. Sélectionnez chaque package (registry-server, api-gateway, etc.)
3. Cliquez sur "Package settings"
4. Faites défiler jusqu'à "Danger Zone"
5. Cliquez sur "Change visibility" > "Public"

**Note :** Gardez-les privées si votre projet contient du code propriétaire.

## Permissions GitHub

Assurez-vous que les GitHub Actions ont les permissions nécessaires :

1. Allez dans **Settings** > **Actions** > **General**
2. Dans "Workflow permissions", sélectionnez :
   - ✅ "Read and write permissions"
   - ✅ "Allow GitHub Actions to create and approve pull requests"

## Préparation de la VM Azure

Avant le premier déploiement, assurez-vous que votre VM Azure :

1. **Est accessible via SSH**
   ```bash
   ssh azureuser@<VM_IP>
   ```

2. **A Docker installé**
   ```bash
   docker --version
   docker-compose --version
   ```

3. **A les ports ouverts** dans le Network Security Group Azure :
   - 22 (SSH)
   - 80 (HTTP)
   - 443 (HTTPS)
   - 8080 (API Gateway)
   - 8088 (Keycloak)
   - 8761 (Eureka)
   - 9000 (SonarQube)

4. **A le répertoire de déploiement créé**
   ```bash
   mkdir -p ~/ecom-app
   ```

## Premier déploiement

1. **Configurez tous les secrets** dans GitHub (voir ci-dessus)

2. **Mergez votre code sur la branche release**
   ```bash
   git checkout release
   git merge feature/deployment
   git push origin release
   ```

3. **Surveillez le déploiement**
   - Allez dans l'onglet **Actions** de votre repository
   - Sélectionnez le workflow en cours
   - Suivez les logs en temps réel

4. **Vérifiez le déploiement**
   - Frontend: http://<VM_IP>
   - API Gateway: http://<VM_IP>:8080
   - Eureka: http://<VM_IP>:8761
   - Keycloak: http://<VM_IP>:8088

## Déploiements ultérieurs

Pour les déploiements suivants, il suffit de :

```bash
# Faire vos modifications sur une branche feature
git checkout -b feature/nouvelle-fonctionnalite
# ... faire vos modifications ...
git commit -am "Nouvelle fonctionnalité"
git push origin feature/nouvelle-fonctionnalite

# Merger sur release
git checkout release
git merge feature/nouvelle-fonctionnalite
git push origin release

# Le déploiement se fait automatiquement ! 🚀
```

## Rollback en cas de problème

Si un déploiement échoue :

1. **Revenir à la version précédente**
   ```bash
   git checkout release
   git revert HEAD
   git push origin release
   ```

2. **Ou se connecter à la VM et revenir manuellement**
   ```bash
   ssh azureuser@<VM_IP>
   cd ~/ecom-app
   
   # Utiliser une image avec un tag spécifique
   # Modifier .env pour changer GITHUB_REPOSITORY_OWNER
   docker-compose -f docker-compose.prod.yml pull
   docker-compose -f docker-compose.prod.yml up -d
   ```

## Monitoring du déploiement

Pour surveiller le déploiement sur la VM :

```bash
# Connexion SSH
ssh azureuser@<VM_IP>

# Voir les logs
cd ~/ecom-app
docker-compose -f docker-compose.prod.yml logs -f

# Voir l'état des conteneurs
docker-compose -f docker-compose.prod.yml ps

# Monitoring
./scripts/monitor.sh
```

## Dépannage

### Problème : Le workflow échoue à "Log in to GitHub Container Registry"

**Solution :** Vérifiez que les permissions GitHub Actions sont correctes (voir section "Permissions GitHub").

### Problème : Le workflow échoue à "Deploy on Azure VM"

**Solutions :**
1. Vérifiez que `AZURE_VM_SSH_KEY` est correctement configuré
2. Testez la connexion SSH manuellement : `ssh azureuser@<VM_IP>`
3. Vérifiez que Docker est installé sur la VM

### Problème : Les conteneurs ne démarrent pas

**Solutions :**
1. Vérifiez les logs : `docker-compose logs`
2. Vérifiez les secrets (mots de passe, clés API)
3. Vérifiez l'espace disque : `df -h`

### Problème : Impossible de pull les images

**Solutions :**
1. Sur la VM, connectez-vous à GHCR manuellement :
   ```bash
   echo $GITHUB_TOKEN | docker login ghcr.io -u <votre-username> --password-stdin
   ```
2. Vérifiez que les images sont bien publiées sur GHCR
3. Si les images sont privées, assurez-vous que le token a les bonnes permissions

## Améliorations possibles

- Ajouter des tests automatisés avant le déploiement
- Implémenter un système de blue-green deployment
- Configurer des notifications Slack/Discord
- Ajouter un système de monitoring avec Prometheus/Grafana
- Implémenter des health checks plus robustes
- Ajouter un système de backup automatique avant déploiement

## Support

En cas de problème :
1. Consultez les logs GitHub Actions
2. Consultez les logs sur la VM : `docker-compose logs`
3. Vérifiez le statut des services : `docker-compose ps`
4. Consultez la documentation Azure pour les problèmes de VM
