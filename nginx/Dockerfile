FROM nginx:alpine

RUN apk add --no-cache openssl bash

RUN mkdir -p /etc/nginx/ssl /var/www/certbot /var/log/nginx

RUN rm -f /etc/nginx/conf.d/default.conf

COPY nginx.conf /etc/nginx/conf.d/app.conf

RUN cat > /docker-entrypoint.d/40-generate-ssl.sh << 'EOF'
#!/bin/sh
set -e

if [ ! -f /etc/nginx/ssl/selfsigned.crt ] || [ ! -f /etc/nginx/ssl/selfsigned.key ]; then
    echo "🔐 Génération des certificats SSL auto-signés..."
    openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
        -keyout /etc/nginx/ssl/selfsigned.key \
        -out /etc/nginx/ssl/selfsigned.crt \
        -subj "/C=FR/ST=IDF/L=Paris/O=MyCompany/OU=IT/CN=*.cloudapp.azure.com"
    echo "Certificats SSL générés"
else
    echo "Certificats SSL déjà présents"
fi

ls -lh /etc/nginx/ssl/
EOF

RUN chmod +x /docker-entrypoint.d/40-generate-ssl.sh

EXPOSE 80 443

CMD ["nginx", "-g", "daemon off;"]