#!/bin/bash
# Bash script to run the application with environment variables loaded
# Usage: ./run-dev.sh

echo "=================================="
echo "E-Commerce Backend - Development"
echo "=================================="
echo ""

ENV_FILE=".env"

# Check if .env file exists
if [ ! -f "$ENV_FILE" ]; then
    echo "ERROR: .env file not found!"
    echo "Please create .env file from .env.example:"
    echo "  cp .env.example .env"
    exit 1
fi

echo "Loading environment variables from $ENV_FILE..."

# Export all variables from .env file
set -a
source "$ENV_FILE"
set +a

echo ""
echo "Starting Spring Boot application..."
echo ""

# Run Maven
mvn spring-boot:run

# Check exit code
if [ $? -ne 0 ]; then
    echo ""
    echo "Application failed to start!"
    exit 1
fi
