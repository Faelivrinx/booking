package com.dominikdev.booking.offer

/**
 * # Offer Management Bounded Context
 *
 * ## Overview
 * Manages business profiles, services, and staff assignments.
 * Defines "what" a business offers and "who" can deliver those services.
 *
 * **Core Responsibilities:**
 * - Business profile management (basic info: name, address, contact)
 * - Service catalog (services with fixed pricing)
 * - Staff management and service assignments
 *
 * **Key Rules:**
 * - Business owners control services and staff
 * - Staff can self-assign/unassign to existing services
 * - Staff deactivation is atomic, cascading handled by other contexts
 */
