Here's the updated list of available API endpoints for your booking system:

# Booking System API Reference

## Business Profile Management
- `POST /api/businesses` - Create a new business with owner account
- `GET /api/businesses/{businessId}` - Retrieve a business profile
- `PUT /api/businesses/{businessId}` - Update a business profile

## Business Identity Management
- `POST /api/identity/businesses` - Create a new business identity
- `PUT /api/identity/businesses/{businessId}` - Update a business identity

## Service Management
- `GET /api/businesses/{businessId}/services` - Get all services for a business
- `GET /api/businesses/{businessId}/services/{serviceId}` - Get a specific service
- `POST /api/businesses/{businessId}/services` - Add a new service
- `PUT /api/businesses/{businessId}/services/{serviceId}` - Update a service
- `DELETE /api/businesses/{businessId}/services/{serviceId}` - Delete a service

## Staff Management
- `GET /api/businesses/{businessId}/staff` - Get all staff members
- `GET /api/businesses/{businessId}/staff/{staffId}` - Get a specific staff member
- `POST /api/businesses/{businessId}/staff` - Create a new staff member
- `POST /api/businesses/{businessId}/staff/{staffId}/deactivate` - Deactivate a staff member
- `PUT /api/businesses/{businessId}/staff/profile` - Update authenticated staff member's profile

## Staff Service Assignments
- `GET /api/businesses/{businessId}/staff/{staffId}/services` - Get services a staff member can perform
- `POST /api/businesses/{businessId}/staff/{staffId}/services` - Set exact services for a staff member
- `POST /api/businesses/{businessId}/staff/{staffId}/services/{serviceId}` - Assign a service to staff
- `DELETE /api/businesses/{businessId}/staff/{staffId}/services/{serviceId}` - Remove a service from staff

## Staff Availability Management
- `POST /api/businesses/{businessId}/staff/{staffId}/availability/{date}` - Set staff availability for a date
- `GET /api/businesses/{businessId}/staff/{staffId}/availability/{date}` - Get staff availability for a date
- `POST /api/businesses/{businessId}/staff/{staffId}/availability/{date}/slots` - Add a time slot to staff availability
- `DELETE /api/businesses/{businessId}/staff/{staffId}/availability/{date}/slots` - Remove a time slot from availability

## Available Booking Slots
- `GET /api/businesses/{businessId}/available-slots/service/{serviceId}` - Get available slots for a service
- `GET /api/businesses/{businessId}/available-slots/staff/{staffId}/service/{serviceId}` - Get available slots for a staff member and service
- `GET /api/businesses/{businessId}/available-slots/service/{serviceId}/days-with-slots` - Get days with available slots for a service

## Client Identity Management
- `POST /api/identity/clients/register` - Register a new client
- `POST /api/identity/clients/activate` - Activate a client account
- `POST /api/identity/clients/resend-code` - Resend verification code to client