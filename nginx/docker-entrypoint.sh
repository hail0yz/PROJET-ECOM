#!/bin/sh

set -e

echo "Initialisation des certificats SSL..."

mkdir -p /etc/nginx/ssl

if [ ! -f /etc/nginx/ssl/selfsigned.crt ] || [ ! -f /etc/nginx/ssl/selfsigned.key ]; then
    echo "Génération des certificats auto-signés..."
    
    openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
        -keyout /etc/nginx/ssl/selfsigned.key \
        -out /etc/nginx/ssl/selfsigned.crt \
        -subj "/C=FR/ST=IDF/L=Paris/O=MyCompany/OU=IT/CN=*.cloudapp.azure.com"

    if [ -f /etc/nginx/ssl/selfsigned.crt ] && [ -f /etc/nginx/ssl/selfsigned.key ]; then
        echo "Certificats SSL générés avec succès"
        ls -lh /etc/nginx/ssl/
    else
        echo "Erreur : Les certificats n'ont pas été créés"
        exit 1
    fi
else
    echo "Certificats SSL déjà présents"
    ls -lh /etc/nginx/ssl/
fi

echo "Test de la configuration nginx..."
nginx -t

echo "Démarrage de nginx..."

exec nginx -g "daemon off;"