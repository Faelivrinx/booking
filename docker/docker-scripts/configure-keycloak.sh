#!/bin/bash

set -e

# Colors for pretty output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Configuring Keycloak...${NC}"

# Wait for Keycloak to start
echo -e "${YELLOW}Waiting for Keycloak to start...${NC}"
until curl -s http://localhost:8080/health/ready > /dev/null; do
    echo -n "."
    sleep 2
done
echo -e "\n${GREEN}Keycloak is up and running${NC}"

# Get admin token
echo -e "${YELLOW}Getting admin token...${NC}"
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/realms/master/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin" \
  -d "password=admin" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" | jq -r '.access_token')

if [ -z "$ADMIN_TOKEN" ]; then
    echo -e "${RED}Failed to get admin token${NC}"
    exit 1
fi

# Create realm
echo -e "${YELLOW}Creating appointment-realm...${NC}"
curl -s -X POST http://localhost:8080/admin/realms \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "realm": "appointment-realm",
    "enabled": true,
    "displayName": "Appointment System",
    "registrationAllowed": false,
    "resetPasswordAllowed": true,
    "loginWithEmailAllowed": true,
    "duplicateEmailsAllowed": false,
    "sslRequired": "external"
  }' || echo -e "${YELLOW}Realm may already exist${NC}"

# Get new token for created realm
echo -e "${YELLOW}Getting token for new realm...${NC}"
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/realms/master/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin" \
  -d "password=admin" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" | jq -r '.access_token')

# Create client
echo -e "${YELLOW}Creating appointment-client...${NC}"
CLIENT_ID=$(curl -s -X POST http://localhost:8080/admin/realms/appointment-realm/clients \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "appointment-client",
    "enabled": true,
    "publicClient": false,
    "redirectUris": ["http://localhost:8081/*"],
    "webOrigins": ["http://localhost:8081"],
    "directAccessGrantsEnabled": true,
    "serviceAccountsEnabled": true,
    "authorizationServicesEnabled": true,
    "protocol": "openid-connect",
    "standardFlowEnabled": true,
    "clientAuthenticatorType": "client-secret"
  }' -v 2>&1 | grep "Location" | cut -d'/' -f 7 | tr -d '\r')

# If client already exists, find its ID
if [ -z "$CLIENT_ID" ]; then
    echo -e "${YELLOW}Client may already exist, fetching ID...${NC}"
    CLIENT_ID=$(curl -s http://localhost:8080/admin/realms/appointment-realm/clients \
      -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.[] | select(.clientId=="appointment-client") | .id')
fi

if [ -z "$CLIENT_ID" ]; then
    echo -e "${RED}Failed to create or find client${NC}"
    exit 1
fi

# Get client secret
CLIENT_SECRET=$(curl -s http://localhost:8080/admin/realms/appointment-realm/clients/$CLIENT_ID/client-secret \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.value')

if [ -z "$CLIENT_SECRET" ]; then
    echo -e "${YELLOW}Generating new client secret...${NC}"
    curl -s -X POST http://localhost:8080/admin/realms/appointment-realm/clients/$CLIENT_ID/client-secret \
      -H "Authorization: Bearer $ADMIN_TOKEN" > /dev/null

    CLIENT_SECRET=$(curl -s http://localhost:8080/admin/realms/appointment-realm/clients/$CLIENT_ID/client-secret \
      -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.value')
fi

# Create BUSINESS role
echo -e "${YELLOW}Creating BUSINESS role...${NC}"
curl -s -X POST http://localhost:8080/admin/realms/appointment-realm/roles \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "BUSINESS",
    "description": "Role for business owners"
  }' || echo -e "${YELLOW}Role may already exist${NC}"

# Create CLIENT role
echo -e "${YELLOW}Creating CLIENT role...${NC}"
curl -s -X POST http://localhost:8080/admin/realms/appointment-realm/roles \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "CLIENT",
    "description": "Role for clients/customers"
  }' || echo -e "${YELLOW}Role may already exist${NC}"

echo -e "${GREEN}Keycloak configuration completed:${NC}"
echo -e "  Realm: appointment-realm"
echo -e "  Client ID: appointment-client"
echo -e "  Client Secret: ${CLIENT_SECRET}"
echo -e ""
echo -e "${YELLOW}Update your application.yml with these values${NC}"
echo -e "${YELLOW}Or use these environment variables:${NC}"
echo -e "  KEYCLOAK_URL=http://localhost:8080"
echo -e "  KEYCLOAK_REALM=appointment-realm"
echo -e "  KEYCLOAK_CLIENT_ID=appointment-client"
echo -e "  KEYCLOAK_CLIENT_SECRET=${CLIENT_SECRET}"
echo -e "  KEYCLOAK_ADMIN_USERNAME=admin"
echo -e "  KEYCLOAK_ADMIN_PASSWORD=admin"
echo -e "  SERVER_PORT=8081"