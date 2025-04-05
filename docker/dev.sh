#!/bin/bash

# Colors for pretty output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to display help message
function show_help {
  echo -e "${YELLOW}Booking Service Development Helper${NC}"
  echo ""
  echo "Usage: ./dev.sh [command]"
  echo ""
  echo "Commands:"
  echo "  start       - Start Docker infrastructure (Postgres & Keycloak)"
  echo "  stop        - Stop Docker infrastructure"
  echo "  restart     - Restart Docker infrastructure"
  echo "  logs        - Show logs from Docker containers"
  echo "  ps          - Show running containers"
  echo "  clean       - Stop containers and remove volumes (WILL DELETE ALL DATA)"
  echo "  keycloak    - Open Keycloak admin console in browser"
  echo "  db-connect  - Connect to PostgreSQL with psql"
  echo "  help        - Show this help message"
}

# Check if Docker is running
function check_docker {
  if ! docker info >/dev/null 2>&1; then
    echo -e "${RED}Error: Docker is not running${NC}"
    exit 1
  fi
}

# Start Docker infrastructure
function start_infra {
  check_docker
  echo -e "${YELLOW}Starting infrastructure...${NC}"

  # Ensure script directory exists
  mkdir -p docker-scripts

  # Make sure init script is executable
  if [ -f docker-scripts/create-multiple-postgresql-databases.sh ]; then
    chmod +x docker-scripts/create-multiple-postgresql-databases.sh
  else
    echo -e "${RED}Error: Database initialization script not found${NC}"
    echo "Please make sure docker-scripts/create-multiple-postgresql-databases.sh exists"
    exit 1
  fi

  docker compose up -d
  echo -e "${GREEN}Infrastructure started:${NC}"
  echo -e "  - PostgreSQL: localhost:5432"
  echo -e "  - Keycloak: http://localhost:8080"
  echo ""
  echo -e "${YELLOW}Keycloak admin credentials:${NC}"
  echo "  - Username: admin"
  echo "  - Password: admin"
}

# Execute command based on argument
case "$1" in
start)
  start_infra
  ;;
stop)
  check_docker
  echo -e "${YELLOW}Stopping infrastructure...${NC}"
  docker compose down
  echo -e "${GREEN}Infrastructure stopped${NC}"
  ;;
restart)
  check_docker
  echo -e "${YELLOW}Restarting infrastructure...${NC}"
  docker compose down
  start_infra
  ;;
logs)
  check_docker
  docker compose logs -f
  ;;
ps)
  check_docker
  echo -e "${YELLOW}Running containers:${NC}"
  docker compose ps
  ;;
clean)
  check_docker
  echo -e "${RED}WARNING: This will delete all data!${NC}"
  read -p "Are you sure you want to continue? (y/n) " -n 1 -r
  echo
  if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}Stopping infrastructure and removing volumes...${NC}"
    docker compose down -v
    echo -e "${GREEN}Infrastructure cleaned${NC}"
  fi
  ;;
keycloak)
  check_docker
  if ! docker compose ps keycloak | grep -q "Up"; then
    echo -e "${RED}Error: Keycloak is not running${NC}"
    echo "Start the infrastructure first with: ./dev.sh start"
    exit 1
  fi
  echo -e "${YELLOW}Opening Keycloak admin console...${NC}"
  open http://localhost:8080/admin/ 2>/dev/null || xdg-open http://localhost:8080/admin/ 2>/dev/null || echo -e "${YELLOW}Please open http://localhost:8080/admin/ in your browser${NC}"
  ;;
db-connect)
  check_docker
  if ! docker compose ps postgres | grep -q "Up"; then
    echo -e "${RED}Error: PostgreSQL is not running${NC}"
    echo "Start the infrastructure first with: ./dev.sh start"
    exit 1
  fi
  echo -e "${YELLOW}Connecting to PostgreSQL...${NC}"
  echo -e "Available databases: appointment_db, keycloak_db"
  read -p "Which database do you want to connect to? [appointment_db] " DB_NAME
  DB_NAME=${DB_NAME:-appointment_db}
  docker exec -it booking-postgres psql -U postgres -d $DB_NAME
  ;;
help | *)
  show_help
  ;;
esac
