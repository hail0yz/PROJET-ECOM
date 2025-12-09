FROM nginx:alpine

RUN apk add --no-cache openssl bash

RUN mkdir -p /etc/nginx/ssl /var/www/certbot /var/log/nginx

RUN rm -f /etc/nginx/conf.d/default.conf

COPY nginx.conf /etc/nginx/conf.d/app.conf

COPY docker-entrypoint.sh /docker-entrypoint.sh

RUN chmod +x /docker-entrypoint.sh

EXPOSE 80 443

ENTRYPOINT ["/docker-entrypoint.sh"]