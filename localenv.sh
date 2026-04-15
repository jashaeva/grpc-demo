#!/bin/bash

# Останавливаем и удаляем существующие контейнеры
docker stop $(docker ps -a -q) 2>/dev/null || true
docker rm $(docker ps -a -q) 2>/dev/null || true

# Если надо - через запятую добавить еще БД
docker run --name grpc-example \
  -p 5437:5432 \
  -e POSTGRES_PASSWORD=secret \
  -e POSTGRES_USER=postgres \
  -v pgdata:/var/lib/postgresql/data \
  -v ./postgres/script:/docker-entrypoint-initdb.d \
  -e CREATE_DATABASES=inventorydb \
  -e TZ=GMT+3 \
  -e PGTZ=GMT+3 \
  -d postgres:15.1 \
  --max_prepared_transactions=100

echo "Waiting for PostgreSQL to start..."
sleep 10

echo "PostgreSQL is ready on port 5437"
