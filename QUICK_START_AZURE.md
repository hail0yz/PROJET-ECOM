# Guide de déploiement rapide sur Azure VM

## Étape 1: Créer une VM Azure

```bash
# Via Azure CLI
az vm create \
  --resource-group ecom-rg \
  --name ecom-vm \
  --image Ubuntu2204 \
  --size Standard_D4s_v3 \
  --admin-username azureuser \
  --generate-ssh-keys

# Ouvrir les ports
az vm open-port --port 80 --resource-group ecom-rg --name ecom-vm --priority 1001
az vm open-port --port 443 --resource-group ecom-rg --name ecom-vm --priority 1002
az vm open-port --port 8080 --resource-group ecom-rg --name ecom-vm --priority 1003
az vm open-port --port 8088 --resource-group ecom-rg --name ecom-vm --priority 1004
az vm open-port --port 8761 --resource-group ecom-rg --name ecom-vm --priority 1005
```

## Étape 2: Déploiement automatisé

Sur votre machine locale:

```bash
# Rendre le script exécutable
chmod +x deploy-to-azure.sh

# Configurer .env
cp .env.example .env
nano .env  # Configurer vos variables

# Lancer le déploiement
./deploy-to-azure.sh <IP_VM> azureuser
```

## Étape 3: Accéder à l'application

- Frontend: http://<IP_VM>
- API Gateway: http://<IP_VM>:8080
- Eureka: http://<IP_VM>:8761
- Keycloak: http://<IP_VM>:8088/admin

## Scripts de gestion

```bash
# Sur la VM
cd ~/apps/PROJET-ECOM

# Démarrer
./scripts/start.sh

# Arrêter
./scripts/stop.sh

# Redémarrer
./scripts/restart.sh

# Voir les logs
./scripts/logs.sh

# Monitoring
./scripts/monitor.sh

# Sauvegarder
./scripts/backup.sh

# Mettre à jour
./scripts/update.sh
```

Pour plus de détails, consultez `AZURE_DEPLOYMENT.md`
