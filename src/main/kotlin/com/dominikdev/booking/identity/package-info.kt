package com.dominikdev.booking.identity


/**
 * # Identity Bounded Context
 *
 * ## Overview
 * The Identity context is responsible for user account management and authentication integration.
 * It acts as a bridge between Keycloak (authentication/authorization) and our application's
 * business user data.
 *
 * ## Architecture
 *
 * ### Hybrid Authentication Model
 * - **Keycloak**: Handles authentication, password management, email verification, JWT tokens
 * - **Local Database**: Stores additional business profile data (phone, business associations)
 * - **Synchronization**: Each user profile linked via `keycloakId`
 *
 * ## Supported User Types
 *
 * ### 1. Business Owner
 * - **Creation**: Manual (admin-only process)
 * - **Purpose**: Manages business profile, services, employees
 * - **Attributes**: Linked to specific business via `businessId`
 * - **Keycloak Role**: `BUSINESS_OWNER`
 *
 * ### 2. Employee
 * - **Creation**: By business owner
 * - **Purpose**: Manages own schedule, views assigned reservations
 * - **Attributes**: Linked to business via `businessId`
 * - **Keycloak Role**: `EMPLOYEE`
 * - **Initial Setup**: Temporary password, forced change on first login
 *
 * ### 3. Client
 * - **Creation**: Self-registration
 * - **Purpose**: Books appointments, manages own reservations
 * - **Attributes**: No business association
 * - **Keycloak Role**: `CLIENT`
 *
 * ## API Endpoints
 *
 * ### Public Endpoints
 * - `POST /api/identity/clients/register` - Client self-registration
 * - `POST /api/identity/password-reset` - Password reset request
 *
 * ### Admin Endpoints
 * - `POST /api/identity/business-owners` - Create business owner account
 *
 * ### Business Owner Endpoints
 * - `POST /api/identity/employees` - Create employee account
 * - `POST /api/identity/employees/{id}/deactivate` - Deactivate employee
 *
 * ### Authenticated User Endpoints
 * - `GET /api/identity/profile` - Get current user profile
 * - `PUT /api/identity/profile` - Update current user profile
 * - `GET /api/identity/attributes` - Get JWT attributes (businessId, etc.)
 *
 * ### Security Integration
 * - **JWT Validation**: Automatic validation via Spring Security
 * - **Role-based Access**: `@PreAuthorize` annotations
 * - **CORS Support**: Configured for cross-origin requests
 * - **Custom Role Converter**: Extracts roles from Keycloak JWT tokens
 */