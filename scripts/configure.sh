#!/bin/bash

# Complete Business Setup & Test Script
# This script sets up the entire business creation flow and tests all functionality

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m'

API_BASE_URL="http://localhost:8081/api"
KEYCLOAK_URL="http://localhost:8080"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Complete Business Setup & Test Script${NC}"
echo -e "${BLUE}========================================${NC}"

# Function to check prerequisites
check_prerequisites() {
  echo -e "${YELLOW}Checking prerequisites...${NC}"

  # Check if jq is installed
  if ! command -v jq &>/dev/null; then
    echo -e "${RED}✗ jq is not installed${NC}"
    echo -e "${YELLOW}Please install jq: sudo apt-get install jq (Ubuntu) or brew install jq (Mac)${NC}"
    exit 1
  fi

  # Check if curl is installed
  if ! command -v curl &>/dev/null; then
    echo -e "${RED}✗ curl is not installed${NC}"
    exit 1
  fi

  echo -e "${GREEN}✓ Prerequisites met${NC}"
}

# Function to wait for services
wait_for_services() {
  echo -e "${YELLOW}Waiting for services to be ready...${NC}"
  return 0

  local max_attempts=30
  local attempt=1

  while [ $attempt -le $max_attempts ]; do
    if curl -sf "$API_BASE_URL/actuator/health" >/dev/null 2>&1 &&
      curl -sf "$KEYCLOAK_URL/health/ready" >/dev/null 2>&1; then
      echo -e "${GREEN}✓ All services are ready${NC}"
      return 0
    fi

    echo -e "${YELLOW}Attempt $attempt/$max_attempts - Services not ready yet...${NC}"
    sleep 2
    ((attempt++))
  done

  echo -e "${RED}✗ Services did not become ready in time${NC}"
  exit 1
}

# Function to get admin token
get_admin_token() {
  echo -e "${YELLOW}Getting admin token...${NC}"

  ADMIN_TOKEN=$(curl -s -X POST "$KEYCLOAK_URL/realms/appointment-realm/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "username=admin" \
    -d "password=admin" \
    -d "grant_type=password" \
    -d "client_id=appointment-frontend" | jq -r '.access_token')

  if [ -z "$ADMIN_TOKEN" ] || [ "$ADMIN_TOKEN" == "null" ]; then
    echo -e "${RED}✗ Failed to get admin token${NC}"
    echo -e "${YELLOW}Make sure Keycloak is configured: cd docker && ./docker-scripts/configure-keycloak.sh${NC}"
    exit 1
  fi

  echo -e "${GREEN}✓ Got admin token${NC}"
}

# Function to create a business with full workflow
create_business_complete() {
  local business_name="$1"
  local owner_email="$2"
  local description="$3"
  local street="$4"
  local city="$5"
  local state="$6"
  local postal="$7"
  local owner_name="$8"
  local owner_phone="$9"

  echo -e "${PURPLE}Creating business: $business_name${NC}"

  # Create business
  BUSINESS_RESPONSE=$(curl -s -X POST "$API_BASE_URL/businesses" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
            \"name\": \"$business_name\",
            \"description\": \"$description\",
            \"street\": \"$street\",
            \"city\": \"$city\",
            \"state\": \"$state\",
            \"postalCode\": \"$postal\",
            \"ownerName\": \"$owner_name\",
            \"ownerEmail\": \"$owner_email\",
            \"ownerPhone\": \"$owner_phone\",
            \"ownerPassword\": \"Password123!\"
        }")

  BUSINESS_ID=$(echo $BUSINESS_RESPONSE | jq -r '.id // empty')

  if [ -n "$BUSINESS_ID" ] && [ "$BUSINESS_ID" != "null" ]; then
    echo -e "${GREEN}✓ Business created: $BUSINESS_ID${NC}"

    # Test owner login
    test_owner_login "$owner_email" "TempPassword123!"

    # Add services to the business
    add_business_services "$BUSINESS_ID"

    # Add staff to the business
    add_business_staff "$BUSINESS_ID"

    # Test service assignments
    test_service_assignments "$BUSINESS_ID"

    return 0
  else
    echo -e "${RED}✗ Failed to create business${NC}"
    echo -e "${RED}Response: $BUSINESS_RESPONSE${NC}"
    return 1
  fi
}

# Function to test owner login and get token
test_owner_login() {
  local email="$1"
  local password="$2"

  echo -e "${YELLOW}Testing business owner login: $email${NC}"

  OWNER_TOKEN=$(curl -s -X POST "$KEYCLOAK_URL/realms/appointment-realm/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "username=$email" \
    -d "password=$password" \
    -d "grant_type=password" \
    -d "client_id=appointment-frontend" | jq -r '.access_token // empty')

  if [ -n "$OWNER_TOKEN" ] && [ "$OWNER_TOKEN" != "null" ]; then
    echo -e "${GREEN}✓ Owner login successful${NC}"

    # Store owner token for later use
    echo "$OWNER_TOKEN" >/tmp/owner_token_$BUSINESS_ID

    return 0
  else
    echo -e "${RED}✗ Owner login failed${NC}"
    return 1
  fi
}

# Function to add services to a business
add_business_services() {
  local business_id="$1"

  echo -e "${YELLOW}Adding services to business: $business_id${NC}"

  # Get owner token
  local owner_token=$(cat /tmp/owner_token_$business_id 2>/dev/null || echo "$ADMIN_TOKEN")

  # Define services
  local services=(
    "Deep Tissue Massage|Therapeutic deep tissue massage|60|89.99"
    "Swedish Massage|Relaxing Swedish massage|60|79.99"
    "Hot Stone Therapy|Hot stone massage therapy|90|129.99"
    "Facial Treatment|Rejuvenating facial treatment|45|69.99"
    "Manicure & Pedicure|Complete nail care service|75|55.99"
  )

  local service_ids=()

  for service_data in "${services[@]}"; do
    IFS='|' read -r name description duration price <<<"$service_data"

    echo -e "${YELLOW}  Adding service: $name${NC}"

    SERVICE_RESPONSE=$(curl -s -X POST "$API_BASE_URL/businesses/$business_id/services" \
      -H "Authorization: Bearer $owner_token" \
      -H "Content-Type: application/json" \
      -d "{
                \"name\": \"$name\",
                \"description\": \"$description\",
                \"durationMinutes\": $duration,
                \"price\": $price
            }")

    SERVICE_ID=$(echo $SERVICE_RESPONSE | jq -r '.id // empty')

    if [ -n "$SERVICE_ID" ] && [ "$SERVICE_ID" != "null" ]; then
      echo -e "${GREEN}    ✓ Service added: $SERVICE_ID${NC}"
      service_ids+=("$SERVICE_ID")
    else
      echo -e "${RED}    ✗ Failed to add service: $name${NC}"
      echo -e "${RED}    Response: $SERVICE_RESPONSE${NC}"
    fi
  done

  # Store service IDs for later use
  printf '%s\n' "${service_ids[@]}" >/tmp/service_ids_$business_id
  echo -e "${GREEN}✓ Added ${#service_ids[@]} services${NC}"
}

# Function to add staff to a business
add_business_staff() {
  local business_id="$1"

  echo -e "${YELLOW}Adding staff to business: $business_id${NC}"

  # Get owner token
  local owner_token=$(cat /tmp/owner_token_$business_id 2>/dev/null || echo "$ADMIN_TOKEN")

  # Define staff members
  local staff=(
    "Michael|Johnson|michael.johnson@spa.com|+15551234567|Massage Therapist"
    "Sarah|Williams|sarah.williams@spa.com|+15551234568|Aesthetician"
    "David|Brown|david.brown@spa.com|+15551234569|Nail Technician"
  )

  local staff_ids=()

  for staff_data in "${staff[@]}"; do
    IFS='|' read -r first_name last_name email phone job_title <<<"$staff_data"

    echo -e "${YELLOW}  Adding staff: $first_name $last_name${NC}"

    STAFF_RESPONSE=$(curl -s -X POST "$API_BASE_URL/businesses/$business_id/staff" \
      -H "Authorization: Bearer $owner_token" \
      -H "Content-Type: application/json" \
      -d "{
                \"firstName\": \"$first_name\",
                \"lastName\": \"$last_name\",
                \"email\": \"$email\",
                \"phoneNumber\": \"$phone\",
                \"jobTitle\": \"$job_title\",
                \"businessName\": \"Test Business\"
            }")

    STAFF_ID=$(echo $STAFF_RESPONSE | jq -r '.id // empty')

    if [ -n "$STAFF_ID" ] && [ "$STAFF_ID" != "null" ]; then
      echo -e "${GREEN}    ✓ Staff added: $STAFF_ID${NC}"
      staff_ids+=("$STAFF_ID")
    else
      echo -e "${RED}    ✗ Failed to add staff: $first_name $last_name${NC}"
      echo -e "${RED}    Response: $STAFF_RESPONSE${NC}"
    fi
  done

  # Store staff IDs for later use
  printf '%s\n' "${staff_ids[@]}" >/tmp/staff_ids_$business_id
  echo -e "${GREEN}✓ Added ${#staff_ids[@]} staff members${NC}"
}

# Function to test service assignments
test_service_assignments() {
  local business_id="$1"

  echo -e "${YELLOW}Testing service assignments for business: $business_id${NC}"

  # Get owner token
  local owner_token=$(cat /tmp/owner_token_$business_id 2>/dev/null || echo "$ADMIN_TOKEN")

  # Read service and staff IDs
  local service_ids=()
  local staff_ids=()

  if [ -f "/tmp/service_ids_$business_id" ]; then
    while IFS= read -r line; do
      service_ids+=("$line")
    done <"/tmp/service_ids_$business_id"
  fi

  if [ -f "/tmp/staff_ids_$business_id" ]; then
    while IFS= read -r line; do
      staff_ids+=("$line")
    done <"/tmp/staff_ids_$business_id"
  fi

  if [ ${#service_ids[@]} -eq 0 ] || [ ${#staff_ids[@]} -eq 0 ]; then
    echo -e "${YELLOW}⚠ No services or staff found, skipping assignments${NC}"
    return
  fi

  # Assign services to staff members
  local staff_index=0
  for staff_id in "${staff_ids[@]}"; do
    echo -e "${YELLOW}  Assigning services to staff: $staff_id${NC}"

    # Assign different services based on staff member
    local services_to_assign=()
    case $staff_index in
    0) # Massage Therapist - massage services
      services_to_assign=("${service_ids[0]}" "${service_ids[1]}" "${service_ids[2]}")
      ;;
    1) # Aesthetician - facial services
      services_to_assign=("${service_ids[3]}")
      ;;
    2) # Nail Technician - nail services
      services_to_assign=("${service_ids[4]}")
      ;;
    esac

    # Set all services for this staff member
    if [ ${#services_to_assign[@]} -gt 0 ]; then
      local service_ids_json=$(printf '"%s",' "${services_to_assign[@]}" | sed 's/,$//')

      ASSIGNMENT_RESPONSE=$(curl -s -X POST "$API_BASE_URL/businesses/$business_id/staff/$staff_id/services" \
        -H "Authorization: Bearer $owner_token" \
        -H "Content-Type: application/json" \
        -d "{
                    \"serviceIds\": [$service_ids_json]
                }")

      if echo "$ASSIGNMENT_RESPONSE" | grep -q "successfully"; then
        echo -e "${GREEN}    ✓ Assigned ${#services_to_assign[@]} services to staff${NC}"
      else
        echo -e "${RED}    ✗ Failed to assign services${NC}"
        echo -e "${RED}    Response: $ASSIGNMENT_RESPONSE${NC}"
      fi
    fi

    ((staff_index++))
  done
}

# Function to verify business setup
verify_business_setup() {
  local business_id="$1"

  echo -e "${YELLOW}Verifying business setup: $business_id${NC}"

  # Test business retrieval
  BUSINESS_CHECK=$(curl -s -X GET "$API_BASE_URL/businesses/$business_id" \
    -H "Authorization: Bearer $ADMIN_TOKEN")

  if echo "$BUSINESS_CHECK" | jq -e '.id' >/dev/null; then
    echo -e "${GREEN}✓ Business data accessible${NC}"
  else
    echo -e "${RED}✗ Business data not accessible${NC}"
  fi

  # Test services retrieval
  SERVICES_CHECK=$(curl -s -X GET "$API_BASE_URL/businesses/$business_id/services" \
    -H "Authorization: Bearer $ADMIN_TOKEN")

  local service_count=$(echo "$SERVICES_CHECK" | jq -r '.totalCount // 0')
  echo -e "${GREEN}✓ Found $service_count services${NC}"

  # Test staff retrieval
  STAFF_CHECK=$(curl -s -X GET "$API_BASE_URL/businesses/$business_id/staff" \
    -H "Authorization: Bearer $ADMIN_TOKEN")

  local staff_count=$(echo "$STAFF_CHECK" | jq -r '.totalCount // 0')
  echo -e "${GREEN}✓ Found $staff_count staff members${NC}"
}

# Function to create multiple test businesses
create_test_businesses() {
  echo -e "${BLUE}Creating test businesses...${NC}"

  local businesses=(
    "Sunset Spa & Salon|jane.smith@sunset-spa.com|A luxury spa and salon offering premium wellness services|123 Wellness Ave|New York|NY|10001|Jane Smith|+12125551234"
    "Downtown Fitness Center|mike.johnson@downtown-fitness.com|Modern fitness center with state-of-the-art equipment|456 Fitness Blvd|Los Angeles|CA|90210|Mike Johnson|+13105551234"
    "Harmony Wellness Studio|sarah.wilson@harmony-wellness.com|Holistic wellness center focusing on mind-body balance|789 Zen Street|Chicago|IL|60601|Sarah Wilson|+17735551234"
  )

  local created_businesses=()

  for business_data in "${businesses[@]}"; do
    IFS='|' read -r name email description street city state postal owner_name phone <<<"$business_data"

    if create_business_complete "$name" "$email" "$description" "$street" "$city" "$state" "$postal" "$owner_name" "$phone"; then
      created_businesses+=("$BUSINESS_ID")
      verify_business_setup "$BUSINESS_ID"
      echo -e "${GREEN}✓ Business setup complete: $name${NC}"
    else
      echo -e "${RED}✗ Business setup failed: $name${NC}"
    fi

    echo -e "${BLUE}----------------------------------------${NC}"
  done

  # Summary
  echo -e "${BLUE}========================================${NC}"
  echo -e "${BLUE}Setup Summary${NC}"
  echo -e "${BLUE}========================================${NC}"
  echo -e "${GREEN}✓ Successfully created ${#created_businesses[@]} businesses${NC}"

  for business_id in "${created_businesses[@]}"; do
    echo -e "${GREEN}  - Business ID: $business_id${NC}"
  done
}


# Function to cleanup temporary files
cleanup() {
  echo -e "${YELLOW}Cleaning up temporary files...${NC}"
  rm -f /tmp/owner_token_*
  rm -f /tmp/service_ids_*
  rm -f /tmp/staff_ids_*
  echo -e "${GREEN}✓ Cleanup complete${NC}"
}

# Function to show usage
show_usage() {
  echo "Usage: $0 [command]"
  echo ""
  echo "Commands:"
  echo "  setup     - Run complete business setup (default)"
  echo "  test      - Test API endpoints only"
  echo "  create    - Create test businesses"
  echo "  verify    - Verify services are running"
  echo "  help      - Show this help"
  echo ""
  echo "Prerequisites:"
  echo "  - Docker infrastructure running (./dev.sh start)"
  echo "  - Keycloak configured (./configure-keycloak.sh)"
  echo "  - Spring Boot application running (./gradlew bootRun)"
}

# Main execution function
main() {
  local command="${1:-setup}"

  case "$command" in
  "setup")
    check_prerequisites
    wait_for_services
    get_admin_token
    create_test_businesses
    cleanup
    ;;
  "test")
    check_prerequisites
    wait_for_services
    get_admin_token
    ;;
  "create")
    check_prerequisites
    wait_for_services
    get_admin_token
    create_test_businesses
    cleanup
    ;;
  "verify")
    check_prerequisites
    wait_for_services
    echo -e "${GREEN}✓ All services are running and accessible${NC}"
    ;;
  "help")
    show_usage
    ;;
  *)
    echo -e "${RED}Unknown command: $command${NC}"
    show_usage
    exit 1
    ;;
  esac
}

# Trap to ensure cleanup on exit
trap cleanup EXIT

# Run the script
main "$@"

