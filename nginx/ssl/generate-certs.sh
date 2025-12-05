#!/bin/sh
set -e

SSL_DIR="/etc/nginx/ssl"
mkdir -p $SSL_DIR

# Si on a déjà un certificat Let's Encrypt monté en volume → on ne fait rien
if [ -f "$SSL_DIR/fullchain.pem" ] && [ -f "$SSL_DIR/privkey.pem" ]; then
    echo "Let's Encrypt certificate found → using it"
    exit 0
fi

# Sinon on génère un certificat auto-signé valable pour :
# - localhost
# - l’IP publique de la VM
# - toutes les IPs privées (pour accès local/réseau)
cat > /tmp/openssl.cnf << EOF
[req]
distinguished_name = req_distinguished_name
x509_extensions = v3_req
prompt = no

[req_distinguished_name]
CN = ${PUBLIC_IP:-localhost}

[v3_req]
subjectAltName = @alt_names

[alt_names]
DNS.1 = localhost
DNS.2 = ${PUBLIC_IP:-localhost}
IP.1 = 127.0.0.1
IP.2 = ${PUBLIC_IP:-127.0.0.1}
IP.3 = 172.16.0.0/12
IP.4 = 192.168.0.0/16
IP.5 = 10.0.0.0/8
EOF

openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout $SSL_DIR/selfsigned.key \
    -out $SSL_DIR/selfsigned.crt \
    -config /tmp/openssl.cnf

echo "Self-signed certificate generated"