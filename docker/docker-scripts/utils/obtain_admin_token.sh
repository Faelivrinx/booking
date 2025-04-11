#!/bin/bash

# Keycloak settings
KEYCLOAK_URL="http://localhost:8080"
REALM="appointment-realm"
CLIENT_ID="appointment-client"
CLIENT_SECRET="V5J07sKjJ2v8vx3CrJa4zO6XT8DHyCxa" # Replace with your actual client secret
USERNAME="admin"
PASSWORD="admin"

# Request token
TOKEN_RESPONSE=$(curl -s -X POST "$KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=$CLIENT_ID" \
  -d "client_secret=$CLIENT_SECRET" \
  -d "grant_type=password" \
  -d "username=$USERNAME" \
  -d "password=$PASSWORD" \
  -d "scope=openid")

# Extract and print token only
ACCESS_TOKEN=$(echo $TOKEN_RESPONSE | grep -o '"access_token":"[^"]*' | sed 's/"access_token":"//g')
echo $ACCESS_TOKEN
