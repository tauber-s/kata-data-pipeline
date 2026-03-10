#!/bin/bash
set -e

# Função para esperar o Postgres
wait_for_postgres() {
  until pg_isready -h "$POSTGRES_HOST" -U "$POSTGRES_USER" -d "$POSTGRES_DB"; do
    echo "Esperando PostgreSQL..."
    sleep 5
  done
}

echo "Iniciando Spark Job..."
wait_for_postgres

# Rodar o JAR
java -jar /app/spark-job.jar