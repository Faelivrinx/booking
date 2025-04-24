package com.dominikdev.booking.shared.infrastructure.security

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.core.annotation.Order
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import java.util.UUID

@Aspect
@Component
@Order(1) // Run this before the method execution
class BusinessResourceValidationAspect(
    private val validator: BusinessResourceValidator
) {
    @Around("@annotation(ValidateStaffBelongsToBusiness)")
    fun validateStaffBelongsToBusiness(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method
        val annotation = method.getAnnotation(ValidateStaffBelongsToBusiness::class.java)

        val businessId = extractBusinessId()
        val staffIdParam = annotation.staffIdParam

        // Extract the staff ID from method parameters
        val staffId = extractParamValue(joinPoint, staffIdParam, UUID::class.java)
            ?: throw BusinessSecurityException("Staff ID parameter not found")

        // Validate that the staff belongs to the business
        validator.validateStaffBelongsToBusiness(staffId, businessId)

        // Proceed with the method execution
        return joinPoint.proceed()
    }

    @Around("@annotation(ValidateServiceBelongsToBusiness)")
    fun validateServiceBelongsToBusiness(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method
        val annotation = method.getAnnotation(ValidateServiceBelongsToBusiness::class.java)

        val businessId = extractBusinessId()
        val serviceIdParam = annotation.serviceIdParam

        // Extract the service ID from method parameters
        val serviceId = extractParamValue(joinPoint, serviceIdParam, UUID::class.java)
            ?: throw BusinessSecurityException("Service ID parameter not found")

        // Validate that the service belongs to the business
        validator.validateServiceBelongsToBusiness(serviceId, businessId)

        // Proceed with the method execution
        return joinPoint.proceed()
    }

    private fun <T> extractParamValue(joinPoint: ProceedingJoinPoint, paramName: String, paramType: Class<T>): T? {
        val signature = joinPoint.signature as MethodSignature
        val paramNames = signature.parameterNames
        val args = joinPoint.args

        for (i in paramNames.indices) {
            if (paramNames[i] == paramName && args[i] != null) {
                if (args[i] is String && paramType == UUID::class.java) {
                    return UUID.fromString(args[i] as String) as T
                }
                return args[i] as? T
            }
        }

        return null
    }

    private fun extractBusinessId(): UUID {
        val authentication = SecurityContextHolder.getContext().authentication

        if (authentication is JwtAuthenticationToken) {
            val token = authentication.token
            val businessId = extractBusinessIdFromToken(token)

            if (businessId != null) {
                return UUID.fromString(businessId)
            }
        }

        throw BusinessSecurityException("Business ID not found in authentication token")
    }

    private fun extractBusinessIdFromToken(token: Jwt): String? {
        // First, check for business_id in the normal claims
        val businessId = token.claims["business_id"] as? String
        if (!businessId.isNullOrBlank()) {
            return businessId
        }

        // Then check in the attributes (common practice in Keycloak)
        @Suppress("UNCHECKED_CAST")
        val attributes = token.claims["attributes"] as? Map<String, Any>
        if (attributes != null) {
            @Suppress("UNCHECKED_CAST")
            val businessIdList = attributes["business_id"] as? List<String>
            if (!businessIdList.isNullOrEmpty()) {
                return businessIdList[0]
            }
        }

        return null
    }
}