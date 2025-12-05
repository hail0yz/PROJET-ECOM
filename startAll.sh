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
)

all_services=(
  "$registry"
  "$api_gateway"
  "$payment"
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

mkdir -p "$ROOT_DIR/out"


###############################################
#   SET VARIABLE ENVIRONMENT
###############################################

echo -e "\e[35m=============== Set variable environment ===============\e[0m"

export STRIPE_API_KEY=sk_test_51SVqfTLyupZqNVJQPV2jdpSmoWBIciV1QjUuRqtaZPyD2yIY92fxgIcoiW37WNZUr9nWTQ37nVJtACQCxa0gygd5000guwynAA

echo -e "\e[35m=============== Set variable environment OK ===============\e[0m"


###############################################
#   START REGISTRY SERVER
###############################################

echo -e "\e[35m=============== Launch registry ===============\e[0m"

cd "$ROOT_DIR/backend/$registry" || exit
nohup java -jar target/*.jar > "../../out/logs-$registry.txt" 2>&1 &
cd - >/dev/null || exit

echo "Wait for registry..."

# Wait that port 8761 is open
until curl -s http://localhost:8761 >/dev/null; do
  echo "   > registry not ready yet..."
  sleep 2
done

echo -e "\e[35m=============== Registry UP ===============\e[0m"


###############################################
#   START API GATEWAY
###############################################

echo -e "\e[35m=============== Launch api gateway ===============\e[0m"

cd "$ROOT_DIR/backend/$api_gateway" || exit
nohup java -jar target/*.jar > "../../out/logs-$api_gateway.txt" 2>&1 &
cd - >/dev/null || exit

sleep 3
echo -e "\e[35m=============== API Gateway UP ===============\e[0m"


###############################################
#   START DOCKER CONTAINERS
###############################################

echo -e "\e[35m=============== Verify that Docker is up ===============\e[0m"
if ! docker info >/dev/null 2>&1; then
    sudo service docker start
    sleep 2 # wait for docker to be fully started
else
    echo "Docker already up"
fi

echo -e "\e[35m=============== Launch Docker containers ===============\e[0m"
docker compose -f $DOCKER_COMPOSE_FILE up -d > "./out/logs-docker-compose.txt" 2>&1

echo -e "\e[35m=============== Docker containers launched ===============\e[0m"


# ###############################################
# #   START OTHER MICROSERVICES
# ###############################################

echo -e "\e[35m=============== Launch microservices ===============\e[0m"

launch_service() {
  service=$1
  echo "Launch $service ..."

  if [[ "$service" == "$payment" ]];
  then 
    cd "$ROOT_DIR/$payment" || exit
  else 
      cd "$ROOT_DIR/backend/$service" || exit
  fi

  nohup java -jar target/*.jar > "$ROOT_DIR/out/log-$service.txt" 2>&1 &
  
  # Attendre le "Started" du service
  while ! grep -q "Started" "$ROOT_DIR/out/log-$service.txt"; do
      sleep 1
  done
  
  echo "$service is ready"
  cd - >/dev/null || exit
}

batch_size=2
i=0
current_batch=()

for s in "${services[@]}"; do
  current_batch+=("$s")
  ((i++))

  # Dès qu’il y a 2 services, on lance ce batch
  if (( i % batch_size == 0 )); then
    for service in "${current_batch[@]}"; do
      launch_service "$service"
    done
    current_batch=()
  fi
done

# Si le dernier batch a moins de 2 services, on le lance aussi
if (( ${#current_batch[@]} > 0 )); then
  for service in "${current_batch[@]}"; do
    launch_service "$service"
  done
fi
