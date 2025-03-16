#!/bin/bash

# Base URL - adjust as needed
BASE_URL="http://localhost:8081/api/clients"

# Colors for terminal output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to print section headers
print_header() {
  echo -e "\n${BLUE}==== $1 ====${NC}\n"
}

# 1. Register a new client
print_header "Register a new client"
REGISTER_RESPONSE=$(curl -s -X POST "${BASE_URL}/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "client1@example.com",
    "phoneNumber": "+12345678901",
    "firstName": "John",
    "lastName": "Doe"
  }')

echo "Response:"
echo $REGISTER_RESPONSE | jq .

# Extract client ID and verification code from the response
CLIENT_ID=$(echo $REGISTER_RESPONSE | jq -r '.id')
VERIFICATION_CODE=$(echo $REGISTER_RESPONSE | jq -r '.verificationCode')

echo -e "${GREEN}Client ID: $CLIENT_ID${NC}"
echo -e "${GREEN}Verification Code: $VERIFICATION_CODE${NC}"

# 2. Activate the client account
print_header "Activate client account"
ACTIVATE_RESPONSE=$(curl -s -X POST "${BASE_URL}/activate" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"client1@example.com\",
    \"verificationCode\": \"$VERIFICATION_CODE\",
    \"password\": \"Secure123!\"
  }")

echo "Response:"
echo $ACTIVATE_RESPONSE | jq .

# 3. Resend verification code (if needed)
print_header "Resend verification code"
curl -s -X POST "${BASE_URL}/resend-code" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "client1@example.com"
  }' | jq .

# 4. Get client by ID
print_header "Get client by ID"
curl -s -X GET "${BASE_URL}/$CLIENT_ID" \
  -H "Content-Type: application/json" | jq .