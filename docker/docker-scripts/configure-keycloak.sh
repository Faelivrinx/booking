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
until curl -s http://localhost:8080/health/ready >/dev/null; do
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

# Create backend client
echo -e "${YELLOW}Creating appointment-client (backend)...${NC}"
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
    -H "Authorization: Bearer $ADMIN_TOKEN" >/dev/null

  CLIENT_SECRET=$(curl -s http://localhost:8080/admin/realms/appointment-realm/clients/$CLIENT_ID/client-secret \
    -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.value')
fi

# Create frontend client
echo -e "${YELLOW}Creating appointment-frontend client...${NC}"
# First check if client already exists
FRONTEND_CLIENT_ID=$(curl -s http://localhost:8080/admin/realms/appointment-realm/clients \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.[] | select(.clientId=="appointment-frontend") | .id')

if [ -z "$FRONTEND_CLIENT_ID" ]; then
  # Client doesn't exist, create it
  curl -s -X POST http://localhost:8080/admin/realms/appointment-realm/clients \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "clientId": "appointment-frontend",
        "enabled": true,
        "publicClient": true,
        "redirectUris": ["http://localhost:3000/*"],
        "webOrigins": ["http://localhost:3000"],
        "directAccessGrantsEnabled": true,
        "serviceAccountsEnabled": false,
        "authorizationServicesEnabled": false,
        "protocol": "openid-connect",
        "standardFlowEnabled": true,
        "implicitFlowEnabled": false,
        "rootUrl": "http://localhost:3000",
        "baseUrl": "/",
        "frontchannelLogout": true
      }'

  # Now get the newly created client ID
  FRONTEND_CLIENT_ID=$(curl -s http://localhost:8080/admin/realms/appointment-realm/clients \
    -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.[] | select(.clientId=="appointment-frontend") | .id')
fi

if [ -z "$FRONTEND_CLIENT_ID" ]; then
  echo -e "${RED}Failed to create or find frontend client${NC}"
  exit 1
else
  echo -e "${GREEN}Frontend client ID: ${FRONTEND_CLIENT_ID}${NC}"
fi

# Add business_id attribute mapper to the frontend client
echo -e "${YELLOW}Adding business_id mapper to frontend client...${NC}"
# First check if mapper already exists
MAPPER_EXISTS=$(curl -s http://localhost:8080/admin/realms/appointment-realm/clients/$FRONTEND_CLIENT_ID/protocol-mappers/models \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.[] | select(.name=="business_id") | .id')

if [ -z "$MAPPER_EXISTS" ]; then
  echo -e "${YELLOW}Creating business_id mapper...${NC}"
  curl -s -X POST http://localhost:8080/admin/realms/appointment-realm/clients/$FRONTEND_CLIENT_ID/protocol-mappers/models \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "name": "business_id",
        "protocol": "openid-connect",
        "protocolMapper": "oidc-usermodel-attribute-mapper",
        "consentRequired": false,
        "config": {
          "userinfo.token.claim": "true",
          "user.attribute": "business_id",
          "id.token.claim": "true",
          "access.token.claim": "true",
          "claim.name": "business_id",
          "jsonType.label": "String"
        }
      }'
  echo -e "${GREEN}Mapper created${NC}"
else
  echo -e "${GREEN}business_id mapper already exists${NC}"
fi

# Create BUSINESS_OWNER role
echo -e "${YELLOW}Creating BUSINESS_OWNER role...${NC}"
curl -s -X POST http://localhost:8080/admin/realms/appointment-realm/roles \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "BUSINESS_OWNER",
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

# Create admin role
echo -e "${YELLOW}Creating admin role...${NC}"
curl -s -X POST http://localhost:8080/admin/realms/appointment-realm/roles \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "ADMIN",
    "description": "Administrator role with full privileges"
  }' || echo -e "${YELLOW}Admin role may already exist${NC}"

# Create STAFF_MEMBER role
echo -e "${YELLOW}Creating STAFF_MEMBER role...${NC}"
curl -s -X POST http://localhost:8080/admin/realms/appointment-realm/roles \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "STAFF_MEMBER",
    "description": "Role for staff members of businesses"
  }' || echo -e "${YELLOW}Role may already exist${NC}"


# Create admin user
echo -e "${YELLOW}Creating admin user...${NC}"
ADMIN_USER_ID=$(curl -s -X POST http://localhost:8080/admin/realms/appointment-realm/users \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@example.com",
    "firstName": "Admin",
    "lastName": "User",
    "enabled": true,
    "emailVerified": true,
    "credentials": [
      {
        "type": "password",
        "value": "admin",
        "temporary": false
      }
    ]
  }' -v 2>&1 | grep "Location" | sed -n 's/.*\/users\/\([^"]*\).*/\1/p' | tr -d '\r')

# If admin user already exists, find its ID
if [ -z "$ADMIN_USER_ID" ]; then
  echo -e "${YELLOW}Admin user may already exist, fetching ID...${NC}"
  ADMIN_USER_ID=$(curl -s http://localhost:8080/admin/realms/appointment-realm/users \
    -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.[] | select(.username=="admin") | .id')
fi

if [ -z "$ADMIN_USER_ID" ]; then
  echo -e "${RED}Failed to create or find admin user${NC}"
  exit 1
fi

# Get admin role ID
ADMIN_ROLE_ID=$(curl -s http://localhost:8080/admin/realms/appointment-realm/roles \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.[] | select(.name=="ADMIN") | .id')

if [ -z "$ADMIN_ROLE_ID" ]; then
  echo -e "${RED}Failed to find admin role${NC}"
  exit 1
fi

# Get BUSINESS_OWNER role ID
BUSINESS_OWNER_ROLE_ID=$(curl -s http://localhost:8080/admin/realms/appointment-realm/roles \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.[] | select(.name=="BUSINESS_OWNER") | .id')

if [ -z "$BUSINESS_OWNER_ROLE_ID" ]; then
  echo -e "${RED}Failed to find BUSINESS_OWNER role${NC}"
  exit 1
fi

# Assign admin role to admin user
echo -e "${YELLOW}Assigning admin roles to admin user...${NC}"
curl -s -X POST http://localhost:8080/admin/realms/appointment-realm/users/$ADMIN_USER_ID/role-mappings/realm \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "[
    {
      \"id\": \"$ADMIN_ROLE_ID\",
      \"name\": \"ADMIN\"
    },
    {
      \"id\": \"$BUSINESS_OWNER_ROLE_ID\",
      \"name\": \"BUSINESS_OWNER\"
    }
  ]"

echo -e "${GREEN}Keycloak configuration completed:${NC}"
echo -e "  Realm: appointment-realm"
echo -e "  Backend Client ID: appointment-client"
echo -e "  Backend Client Secret: ${CLIENT_SECRET}"
echo -e "  Frontend Client ID: appointment-frontend"
echo -e ""
echo -e "${YELLOW}Admin user created:${NC}"
echo -e "  Username: admin"
echo -e "  Password: admin"
echo -e "  Email: admin@example.com"
echo -e ""
echo -e "${YELLOW}Update your application.yml with these values${NC}"
echo -e "${YELLOW}Or use these environment variables:${NC}"
echo -e "  KEYCLOAK_URL=http://localhost:8080"
echo -e "  KEYCLOAK_REALM=appointment-realm"
echo -e "  KEYCLOAK_CLIENT_ID=appointment-client"
echo -e "  KEYCLOAK_CLIENT_SECRET=${CLIENT_SECRET}"
echo -e "  KEYCLOAK_ADMIN_USERNAME=admin"
echo -e "  KEYCLOAK_ADMIN_PASSWORD=admin"
echo -e "  SERVER_PORT=8081"s
