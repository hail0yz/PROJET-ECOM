#!/bin/bash

registry="registry-server"
api_gateway="api-gateway"
payment="payment"

services=(
  "bookService"
  "customer-service"
  "order"
  "cart-service"
  "notification"
  "$payment"
)

all_services=(
  "$registry"
  "$api_gateway"
  "bookService"
  "customer-service"
  "order"
  "cart-service"
  "notification"
)

ROOT_DIR="$(pwd)"


###############################################
#   PACKAGE ALL MICROSERVICES
###############################################

for service in ${all_services[@]}
do
    echo -e "\e[35m   ----- Packaging $service ... -----\e[0m"
    if [[ "$service" == "$payment" ]];
    then 
        cd "$ROOT_DIR/$payment" || exit
    else 
        cd "$ROOT_DIR/backend/$service" || exit
    fi

    mkdir -p "$ROOT_DIR/out"

    if mvn clean package > "$ROOT_DIR/out/build-$service.log" 2>&1;
    then
        echo "$service : BUILD SUCCESS"
    else
        echo "ERROR: Build failed for $service"
        echo "Aborting start process."
        exit 1
    fi
done
