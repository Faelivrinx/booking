package com.dominikdev.booking.shared.infrastructure.security

import org.springframework.security.access.prepost.PreAuthorize

/**
 * Security annotation that ensures the authenticated user:
 * 1. Has the BUSINESS_OWNER role
 * 2. Is the owner of the business whose ID is specified in the path variable
 *
 * @param businessIdParam The name of the path variable containing the business ID
 */
/**
 * Security annotation that ensures the authenticated user:
 * 1. Has the BUSINESS_OWNER role
 * 2. Is the owner of the business whose ID is specified in the path variable
 *
 * @param businessIdParam The name of the path variable containing the business ID
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasRole('BUSINESS_OWNER') and @businessOwnerSecurityEvaluator.isBusinessOwner(authentication, #businessId)")
annotation class BusinessOwnerSecurity