#!/bin/bash

registry="registry-server"
api_gateway="api-gateway"

services=(
  "bookService"
  "customer-service"
  "order"
  "cart-service"
  "notification"
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
DOCKER_COMPOSE_FILE="docker-compose.yml"


echo "Kill ancient java services..."
pkill -f "java -jar" 2>/dev/null
sleep 2


###############################################
#   PACKAGE ALL MICROSERVICES
###############################################

for service in ${all_services[@]}
do
    echo "Packaging $service ..."
    cd "$ROOT_DIR/backend/$service" || exit

    if mvn clean package
    then
        echo "$service : BUILD SUCCESS"
    else
        echo "ERROR: Build failed for $service"
        echo "Aborting start process."
        exit 1
    fi
done


###############################################
#   START REGISTRY SERVER
###############################################

echo "=============== Launch registry ==============="

cd "$ROOT_DIR/backend/$registry" || exit
nohup java -jar target/*.jar > "../../logs-$registry.txt" 2>&1 &
cd - >/dev/null || exit

echo "Wait for registry..."

# Wait that port 8761 is open
until curl -s http://localhost:8761 >/dev/null; do
  echo "   > registry not ready yet..."
  sleep 2
done

echo "=============== Registry UP ==============="


###############################################
#   START API GATEWAY
###############################################

echo "=============== Launch api gateway ==============="

cd "$ROOT_DIR/backend/$api_gateway" || exit
nohup java -jar target/*.jar > "../../logs-$api_gateway.txt" 2>&1 &
cd - >/dev/null || exit

sleep 3
echo "=============== API Gateway UP ==============="


###############################################
#   START DOCKER CONTAINERS
###############################################

echo "=============== Verify that Docker is up ==============="
if ! docker info >/dev/null 2>&1; then
    sudo service docker start
    sleep 2 # wait for docker to be fully started
else
    echo "Docker already up"
fi

echo "=============== Launch Docker containers ==============="
docker compose -f $DOCKER_COMPOSE_FILE up -d > "./logs-docker-compose.txt" 2>&1

echo "=============== Docker containers launched ==============="


# ###############################################
# #   START OTHER MICROSERVICES
# ###############################################

echo "=============== Launch microservices ==============="

for service in "${services[@]}"; do
  echo "Launch $service ..."
  cd "$ROOT_DIR/backend/$service" || exit
  nohup java -jar target/*.jar > "../../logs-$service.txt" 2>&1 &
  cd - >/dev/null || exit
done

echo "Wait for all services..."

for service in "${services[@]}"; do
  echo "  Waiting for $service..."
  until grep -q "Started .*Application" "logs-$service.txt"; do
    sleep 2
  done
  echo "  $service est UP !"
done
