#!/bin/bash

ROOT_DIR="$(pwd)"

chmod u+x packageAll.sh startAll.sh startFront.sh

echo "==================== PACKAGE ALL MICROSERVICES ==================== "
./packageAll.sh
echo "==================== PACKAGE ALL MICROSERVICES OK ==================== "

echo "==================== START ALL MICROSERVICES ==================== "
./startAll.sh
echo "==================== START ALL MICROSERVICES OK ==================== "

echo "==================== LAUNCH FRONTEND ==================== "
./startFront.sh
echo "==================== LAUNCH FRONTEND OK ==================== "
