package com.dominikdev.booking.identity

/**
 * # Identity Bounded Context
 *
 * ## Overview
 * Access Control Layer (ACL) for Keycloak IDP. Provides domain-specific identity operations
 * using Keycloak as the single source of truth for all user data.
 *
 * **Architecture:** Web Layer → Identity Facade → Keycloak
 *
 * **User Roles:**
 * - **ADMIN**: Full system access
 * - **BUSINESS_OWNER**: Manages business, employees, services
 * - **EMPLOYEE**: Manages own schedule, views reservations
 * - **CLIENT**: Books appointments
 *
 * ## Supported Operations
 * - User creation (business owners, employees, clients)
 * - Profile management and updates
 * - Role-based permission checking
 * - Business context validation
 * - JWT token integration
 * - Password reset functionality
 *
 * ## API Endpoints
 *
 * **Public:**
 * - `POST /clients/register` - Client self-registration
 * - `POST /password-reset` - Password reset request
 *
 * **Admin:**
 * - `POST /business-owners` - Create business owner
 * - `GET /users/{keycloakId}` - Get any user
 *
 * **Business Owner:**
 * - `POST /employees` - Create employee
 * - `POST /employees/{keycloakId}/deactivate` - Deactivate employee
 * - `GET /businesses/{businessId}/users` - Get business users
 *
 * **Authenticated:**
 * - `GET /profile` - Get current user profile
 * - `PUT /profile` - Update profile
 * - `GET /roles` - Get user roles
 * - `GET /permissions/{permission}` - Check permission
 */
