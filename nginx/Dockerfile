FROM nginx:alpine

RUN apk add --no-cache openssl

RUN mkdir -p /etc/nginx/ssl /var/www/certbot

RUN openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout /etc/nginx/ssl/selfsigned.key \
    -out /etc/nginx/ssl/selfsigned.crt \
    -subj "/C=FR/ST=IDF/L=Paris/O=MyCompany/OU=IT/CN=*.cloudapp.azure.com"

RUN rm -f /etc/nginx/conf.d/default.conf

COPY nginx.conf /etc/nginx/conf.d/app.conf

RUN mkdir -p /var/log/nginx && \
    touch /var/log/nginx/access.log && \
    touch /var/log/nginx/error.log

EXPOSE 80 443

CMD ["nginx", "-g", "daemon off;"]