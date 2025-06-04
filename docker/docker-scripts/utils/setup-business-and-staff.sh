#!/bin/bash

set -e

# Colors for pretty output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

API_BASE_URL="http://localhost:8081/api"

echo -e "${YELLOW}Setting up test business and staff members...${NC}"

# Step 1: Get admin token from Keycloak
echo -e "${YELLOW}Getting admin token from Keycloak...${NC}"
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/realms/appointment-realm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin" \
  -d "password=admin" \
  -d "grant_type=password" \
  -d "client_id=appointment-client" \
  -d "client_secret=TXd06fHNFjbw7ZJlcXkAFGSMEp9D5HW5" | jq -r '.access_token')

if [ -z "$ADMIN_TOKEN" ]; then
  echo -e "${RED}Failed to get admin token${NC}"
  exit 1
fi

# Step 2: Create a business with owner
echo -e "${YELLOW}Creating a business with owner...${NC}"
BUSINESS_RESPONSE=$(curl -s -X POST "$API_BASE_URL/businesses" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Sunset Spa and Salon",
    "description": "A luxury spa and salon offering a wide range of services",
    "street": "123 Main Street",
    "city": "New York",
    "state": "NY",
    "postalCode": "10001",
    "ownerName": "Jane Smith",
    "ownerEmail": "jane.smith@example.com",
    "ownerPhone": "+12125551234",
    "ownerPassword": "Password123!"
  }')

# Extract business ID from response
BUSINESS_ID=$(echo $BUSINESS_RESPONSE | jq -r '.id')

if [ -z "$BUSINESS_ID" ] || [ "$BUSINESS_ID" == "null" ]; then
  echo -e "${RED}Failed to create business${NC}"
  echo $BUSINESS_RESPONSE
  exit 1
fi

echo -e "${GREEN}Successfully created business with ID: $BUSINESS_ID${NC}"
echo -e "${GREEN}Business Owner credentials:${NC}"
echo -e "  Email: jane.smith@example.com"
echo -e "  Password: Password123!"

# Step 3: Get business owner token
echo -e "${YELLOW}Getting business owner token...${NC}"
OWNER_TOKEN=$(curl -s -X POST http://localhost:8080/realms/appointment-realm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=jane.smith@example.com" \
  -d "password=Password123!" \
  -d "grant_type=password" \
  -d "client_id=appointment-frontend" | jq -r '.access_token')

if [ -z "$OWNER_TOKEN" ]; then
  echo -e "${RED}Failed to get business owner token${NC}"
  exit 1
fi

# Step 4: Add a service to the business
echo -e "${YELLOW}Adding a service to the business...${NC}"
SERVICE_RESPONSE=$(curl -s -X POST "$API_BASE_URL/businesses/$BUSINESS_ID/services" \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Classic Massage",
    "durationMinutes": 60,
    "description": "A relaxing full-body massage",
    "price": 89.99
  }')

SERVICE_ID=$(echo $SERVICE_RESPONSE | jq -r '.id')

if [ -z "$SERVICE_ID" ] || [ "$SERVICE_ID" == "null" ]; then
  echo -e "${RED}Failed to create service${NC}"
  echo $SERVICE_RESPONSE
else
  echo -e "${GREEN}Successfully created service with ID: $SERVICE_ID${NC}"
fi

# Step 5: Add a staff member to the business
echo -e "${YELLOW}Adding a staff member to the business...${NC}"
STAFF_RESPONSE=$(curl -s -X POST "$API_BASE_URL/businesses/$BUSINESS_ID/staff" \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Michael",
    "lastName": "Johnson",
    "email": "michael.johnson@example.com",
    "phoneNumber": "+12125551235",
    "jobTitle": "Massage Therapist",
    "businessName": "Sunset Spa and Salon"
  }')

STAFF_ID=$(echo $STAFF_RESPONSE | jq -r '.id')

if [ -z "$STAFF_ID" ] || [ "$STAFF_ID" == "null" ]; then
  echo -e "${RED}Failed to create staff member${NC}"
  echo $STAFF_RESPONSE
  exit 1
fi

echo -e "${GREEN}Successfully created staff member with ID: $STAFF_ID${NC}"
echo -e "${YELLOW}Check logs for the temporary password that was generated for the staff member.${NC}"

# Step 6: Add a second staff member to the business
echo -e "${YELLOW}Adding a second staff member to the business...${NC}"
STAFF_RESPONSE2=$(curl -s -X POST "$API_BASE_URL/businesses/$BUSINESS_ID/staff" \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Sarah",
    "lastName": "Williams",
    "email": "sarah.williams@example.com",
    "phoneNumber": "+12125551236",
    "jobTitle": "Hair Stylist",
    "businessName": "Sunset Spa and Salon"
  }')

STAFF_ID2=$(echo $STAFF_RESPONSE2 | jq -r '.id')

if [ -z "$STAFF_ID2" ] || [ "$STAFF_ID2" == "null" ]; then
  echo -e "${RED}Failed to create second staff member${NC}"
  echo $STAFF_RESPONSE2
else
  echo -e "${GREEN}Successfully created second staff member with ID: $STAFF_ID2${NC}"
  echo -e "${YELLOW}Check logs for the temporary password that was generated for the staff member.${NC}"
fi

# Summary
echo -e "\n${GREEN}=== Setup Complete ===${NC}"
echo -e "${GREEN}Business created:${NC}"
echo -e "  ID: $BUSINESS_ID"
echo -e "  Name: Sunset Spa and Salon"
echo -e "\n${GREEN}Business Owner:${NC}"
echo -e "  Email: jane.smith@example.com"
echo -e "  Password: Password123!"
echo -e "\n${GREEN}Staff Members:${NC}"
echo -e "  1. Michael Johnson (Massage Therapist) - Check logs for password"
echo -e "  2. Sarah Williams (Hair Stylist) - Check logs for password"
echo -e "\n${YELLOW}Note: Staff members need to log in once to activate their accounts${NC}"
