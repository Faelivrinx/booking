package com.dominikdev.booking.shared.infrastructure.security

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidateStaffBelongsToBusiness(
    val staffIdParam: String = "staffId"
)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidateServiceBelongsToBusiness(
    val serviceIdParam: String = "serviceId"
)