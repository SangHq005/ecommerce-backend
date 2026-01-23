#!/bin/bash
# Bash script to start both frontend and backend for development
# Usage: ./scripts/start-dev.sh

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${GREEN}Starting E-Commerce Development Environment...${NC}"

# Check prerequisites
echo -e "${YELLOW}Checking prerequisites...${NC}"

# Check Java
if command -v java &> /dev/null; then
    java_version=$(java -version 2>&1 | head -n 1)
    echo -e "${GREEN}✓ Java found: $java_version${NC}"
else
    echo -e "${RED}✗ Java not found. Please install Java 17 or higher${NC}"
    exit 1
fi

# Check Node.js
if command -v node &> /dev/null; then
    node_version=$(node --version)
    echo -e "${GREEN}✓ Node.js found: $node_version${NC}"
else
    echo -e "${RED}✗ Node.js not found. Please install Node.js 18 or higher${NC}"
    exit 1
fi

# Check npm
if command -v npm &> /dev/null; then
    npm_version=$(npm --version)
    echo -e "${GREEN}✓ npm found: $npm_version${NC}"
else
    echo -e "${RED}✗ npm not found. Please install npm${NC}"
    exit 1
fi

# Set environment variables for backend
echo -e "\n${YELLOW}Setting environment variables...${NC}"
export SERVER_PORT=8080
export SPRING_PROFILES_ACTIVE=dev

# Generate JWT secret if not exists
if [ -z "$JWT_SECRET_BASE64" ]; then
    echo -e "${YELLOW}Generating JWT secret...${NC}"
    export JWT_SECRET_BASE64=$(openssl rand -base64 32)
    echo -e "${YELLOW}JWT secret generated (temporary - set JWT_SECRET_BASE64 env var for persistence)${NC}"
fi

# Function to cleanup on exit
cleanup() {
    echo -e "\n${YELLOW}Stopping services...${NC}"
    kill $BACKEND_PID 2>/dev/null
    kill $FRONTEND_PID 2>/dev/null
    echo -e "${GREEN}Services stopped.${NC}"
    exit 0
}

# Set trap for cleanup
trap cleanup INT TERM

# Start Backend
echo -e "\n${GREEN}Starting Backend Server...${NC}"
cd ecommerce-backend
./mvnw spring-boot:run &
BACKEND_PID=$!
cd ..

# Wait for backend to start
echo -e "${YELLOW}Waiting for backend to start...${NC}"
max_attempts=30
attempt=0
backend_ready=false

while [ $attempt -lt $max_attempts ] && [ "$backend_ready" = false ]; do
    attempt=$((attempt + 1))
    sleep 2
    if curl -s http://localhost:8080/health > /dev/null 2>&1; then
        backend_ready=true
        echo -e "${GREEN}✓ Backend is ready!${NC}"
    else
        echo -n "."
    fi
done

if [ "$backend_ready" = false ]; then
    echo -e "\n${RED}✗ Backend failed to start. Check the logs.${NC}"
    exit 1
fi

# Start Frontend
echo -e "\n${GREEN}Starting Frontend Server...${NC}"
cd ../Meta-Shop-Web/Frontend
npm install
npm run dev &
FRONTEND_PID=$!
cd ../..

# Wait for frontend to start
echo -e "${YELLOW}Waiting for frontend to start...${NC}"
sleep 5

# Display URLs
echo -e "\n${CYAN}========================================${NC}"
echo -e "${GREEN}Development Environment Started!${NC}"
echo -e "${CYAN}========================================${NC}"
echo ""
echo -e "${YELLOW}Frontend URL:    http://localhost:3000${NC}"
echo -e "${YELLOW}Backend URL:     http://localhost:8080${NC}"
echo -e "${YELLOW}Swagger UI:      http://localhost:8080/swagger-ui/index.html${NC}"
echo -e "${YELLOW}Health Check:    http://localhost:8080/health${NC}"
echo ""
echo -e "${CYAN}Press Ctrl+C to stop all services${NC}"
echo -e "${CYAN}========================================${NC}"

# Keep script running
while true; do
    sleep 1
done