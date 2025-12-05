#!/bin/bash

ROOT_DIR="$(pwd)"

cd "$ROOT_DIR/frontend" || exit
nohup ng serve > "../out/logs-frontend.txt" 2>&1 &

while ! grep -q "http://localhost:4200/";
do
    sleep 1
done

cd - >/dev/null || exit