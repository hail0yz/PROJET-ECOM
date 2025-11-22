# Quick Start

Keycloak avec Docker Compose & Gestion des Rôles Utilisateurs.

> ⚠️ Cette configuration manuelle des rôles est indispensable au fonctionnement du service `customer-service`. Sans elle, le service ne pourra pas interagir correctement avec Keycloak.  
> La configuration générale du Realm est, quant à elle, automatisée via la commande `--import-realm` et le fichier de configuration `keycloak/realm.json`.

---

## Prérequis

- [Docker](https://www.docker.com/) & Docker Compose installés

## Configuration

**1. Se placer à la racine du projet**

```sh
cd PROJECT-ECOM    
```

**2. Démarrer Keycloak** :

- **Option A**: uniquement Keycloak
    ```sh
    docker compose ps -d keycloak
    ```

- **Option B**: tous les services
    ```sh
    docker compose ps -d
    ```
---

### Interface d’administration
**Accès:** http://localhost:8088
- Nom d’utilisateur : admin
- Mot de passe : admin

### Attribution des rôles

1. Aller sur [Realms](http://localhost:8088/admin/master/console/#/master/realms) et sélectionner le realm `ecom`
2. Accéder aux [Clients](http://localhost:8088/admin/master/console/#/ecom/clients)
3. Sélectionner le client `customer-service-client`
4. Aller sur l'onglet `Service accounts roles`
5. Cliquer sur `Assign role` > `Client roles`
6. Selectionnez :
   - `view-users`
   - `query-users`
   - `manage-users`
   - `manage-realm`
   - `query-realm`

### Mise à jour du client secret

1. Onglet Credentials 
2. Cliquer sur Regenerate 
3. Copier le nouveau secret 
4. Mettre à jour dans : `backend/customer-service/src/main/resources/application.yml` la propriété `keycloak.client-secret`:
    ```yml
    keycloak:
        client-secret: <nouveau-secret>
   ```