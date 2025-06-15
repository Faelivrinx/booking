package com.dominikdev.booking.identity.web

import com.dominikdev.booking.identity.*
import com.dominikdev.booking.identity.application.DefaultIdentityFacade
import com.dominikdev.booking.identity.domain.UserRole
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/identity")
@CrossOrigin(origins = ["http://localhost:3000", "http://localhost:8080"])
class IdentityEndpoint(
    private val identityFacade: IdentityFacade,
    private val defaultIdentityFacade: DefaultIdentityFacade // For business-specific operations
) {

    private val logger = LoggerFactory.getLogger(IdentityEndpoint::class.java)

    // Public Registration Endpoints

    @PostMapping("/clients/register")
    fun registerClient(@Valid @RequestBody request: ClientRegistrationRequest): ResponseEntity<*> {
        logger.info("Client registration request for email: ${request.email}")

        return try {
            val result = identityFacade.registerClient(request)
            logger.info("Successfully registered client: ${result.keycloakId}")
            ResponseEntity.status(HttpStatus.CREATED).body(result)
        } catch (e: Exception) {
            logger.error("Client registration failed for email: ${request.email}", e)
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "REGISTRATION_FAILED",
                    message = e.message ?: "Registration failed",
                    details = "Please check your input and try again"
                )
            )
        }
    }

    @PostMapping("/password-reset")
    fun requestPasswordReset(@Valid @RequestBody request: PasswordResetRequest): ResponseEntity<*> {
        logger.info("Password reset request for email: ${request.email}")

        return try {
            identityFacade.requestPasswordReset(request.email)
            ResponseEntity.ok(
                SuccessResponse(
                    message = "Password reset email sent if account exists",
                    details = "Check your email for reset instructions"
                )
            )
        } catch (e: Exception) {
            logger.warn("Password reset request processed for email: ${request.email}")
            // Don't reveal if email exists for security - always return success
            ResponseEntity.ok(
                SuccessResponse(
                    message = "Password reset email sent if account exists",
                    details = "Check your email for reset instructions"
                )
            )
        }
    }

    // Admin-Only Endpoints

    @PostMapping("/business-owners")
    @PreAuthorize("hasRole('ADMIN')")
    fun createBusinessOwner(@Valid @RequestBody request: CreateBusinessOwnerRequest): ResponseEntity<*> {
        logger.info("Admin creating business owner: ${request.email}")

        return try {
            val userAccount = identityFacade.createBusinessOwner(request)
            logger.info("Successfully created business owner: ${userAccount.keycloakId}")
            ResponseEntity.status(HttpStatus.CREATED).body(userAccount)
        } catch (e: Exception) {
            logger.error("Failed to create business owner for email: ${request.email}", e)
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "BUSINESS_OWNER_CREATION_FAILED",
                    message = e.message ?: "Failed to create business owner",
                    details = "Please verify the business information and try again"
                )
            )
        }
    }

    @GetMapping("/users/{keycloakId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun getUserAccount(@PathVariable keycloakId: String): ResponseEntity<*> {
        logger.debug("Admin retrieving user account: $keycloakId")

        return try {
            val userAccount = identityFacade.getClientAccount(keycloakId)
                ?: identityFacade.getEmployeeAccount(keycloakId)
                ?: identityFacade.getBusinessOwner(keycloakId)

            if (userAccount != null) {
                ResponseEntity.ok(userAccount)
            } else {
                ResponseEntity.notFound().build<Any>()
            }
        } catch (e: Exception) {
            logger.error("Failed to retrieve user account: $keycloakId", e)
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "USER_RETRIEVAL_FAILED",
                    message = e.message ?: "Failed to retrieve user",
                    details = "User may not exist or be accessible"
                )
            )
        }
    }

    // Business Owner Endpoints

    @PostMapping("/employees")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    fun createEmployee(@Valid @RequestBody request: CreateEmployeeAccountRequest): ResponseEntity<*> {
        logger.info("Business owner creating employee: ${request.email}")

        return try {
            // Validate business access
            defaultIdentityFacade.validateBusinessAccess(
                request.businessId,
                com.dominikdev.booking.identity.domain.Permission.MANAGE_EMPLOYEES
            )

            val userAccount = identityFacade.createEmployeeAccount(request)
            logger.info("Successfully created employee: ${userAccount.keycloakId}")
            ResponseEntity.status(HttpStatus.CREATED).body(userAccount)
        } catch (e: Exception) {
            logger.error("Failed to create employee for email: ${request.email}", e)
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "EMPLOYEE_CREATION_FAILED",
                    message = e.message ?: "Failed to create employee",
                    details = "Please verify the employee information and permissions"
                )
            )
        }
    }

    @PostMapping("/employees/{keycloakId}/deactivate")
    @PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('ADMIN')")
    fun deactivateEmployee(@PathVariable keycloakId: String): ResponseEntity<*> {
        logger.info("Deactivating employee: $keycloakId")

        return try {
            // For business owners, validate they own the employee
            val currentUser = defaultIdentityFacade.getCurrentUserProfile()
            if (currentUser?.role == UserRole.BUSINESS_OWNER) {
                val employee = identityFacade.getEmployeeAccount(keycloakId)
                if (employee != null) {
                    defaultIdentityFacade.validateBusinessAccess(
                        currentUser.businessId!!,
                        com.dominikdev.booking.identity.domain.Permission.MANAGE_EMPLOYEES
                    )
                }
            }

            identityFacade.deactivateEmployeeAccount(keycloakId)
            logger.info("Successfully deactivated employee: $keycloakId")
            ResponseEntity.ok(
                SuccessResponse(
                    message = "Employee account deactivated successfully",
                    details = "The employee will no longer be able to access the system"
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to deactivate employee: $keycloakId", e)
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "EMPLOYEE_DEACTIVATION_FAILED",
                    message = e.message ?: "Failed to deactivate employee",
                    details = "Please verify permissions and try again"
                )
            )
        }
    }

    @GetMapping("/businesses/{businessId}/users")
    @PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('ADMIN')")
    fun getBusinessUsers(@PathVariable businessId: UUID): ResponseEntity<*> {
        logger.info("Retrieving users for business: $businessId")

        return try {
            val users = defaultIdentityFacade.getBusinessUsers(businessId)
            ResponseEntity.ok(
                BusinessUsersResponse(
                    businessId = businessId,
                    users = users,
                    totalCount = users.size
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to retrieve business users for business: $businessId", e)
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "BUSINESS_USERS_RETRIEVAL_FAILED",
                    message = e.message ?: "Failed to retrieve business users",
                    details = "Please verify permissions and business access"
                )
            )
        }
    }

    @PostMapping("/users/{keycloakId}/assign-business/{businessId}")
    @PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('ADMIN')")
    fun assignUserToBusiness(
        @PathVariable keycloakId: String,
        @PathVariable businessId: UUID
    ): ResponseEntity<*> {
        logger.info("Assigning user $keycloakId to business: $businessId")

        return try {
            defaultIdentityFacade.assignUserToBusiness(keycloakId, businessId)
            ResponseEntity.ok(
                SuccessResponse(
                    message = "User successfully assigned to business",
                    details = "User can now access business resources"
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to assign user $keycloakId to business: $businessId", e)
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "USER_ASSIGNMENT_FAILED",
                    message = e.message ?: "Failed to assign user to business",
                    details = "Please verify permissions and try again"
                )
            )
        }
    }

    // Authenticated User Endpoints

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    fun getCurrentUserProfile(): ResponseEntity<*> {
        logger.debug("Retrieving current user profile")

        return try {
            val userAccount = identityFacade.getCurrentUserAccount()
            if (userAccount != null) {
                ResponseEntity.ok(userAccount)
            } else {
                logger.warn("No user profile found for authenticated user")
                ResponseEntity.notFound().build<Any>()
            }
        } catch (e: Exception) {
            logger.error("Failed to retrieve current user profile", e)
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "PROFILE_RETRIEVAL_FAILED",
                    message = e.message ?: "Failed to retrieve profile",
                    details = "Please ensure you are properly authenticated"
                )
            )
        }
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    fun updateProfile(@Valid @RequestBody request: UpdateProfileRequest): ResponseEntity<*> {
        logger.info("Updating user profile")

        return try {
            val keycloakId = identityFacade.getCurrentKeycloakId()
            if (keycloakId != null) {
                val updatedAccount = identityFacade.updateProfile(keycloakId, request)
                logger.info("Successfully updated profile for user: $keycloakId")
                ResponseEntity.ok(updatedAccount)
            } else {
                logger.warn("No keycloak ID found for authenticated user")
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ErrorResponse(
                        error = "INVALID_AUTHENTICATION",
                        message = "Invalid authentication",
                        details = "Please log in again"
                    )
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to update user profile", e)
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "PROFILE_UPDATE_FAILED",
                    message = e.message ?: "Failed to update profile",
                    details = "Please verify your input and try again"
                )
            )
        }
    }

    @GetMapping("/attributes")
    @PreAuthorize("isAuthenticated()")
    fun getUserAttributes(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<*> {
        logger.debug("Extracting user attributes from JWT")

        return try {
            val attributes = identityFacade.extractUserAttributes(jwt)
            ResponseEntity.ok(attributes)
        } catch (e: Exception) {
            logger.error("Failed to extract user attributes", e)
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "ATTRIBUTE_EXTRACTION_FAILED",
                    message = e.message ?: "Failed to extract attributes",
                    details = "Please ensure your token is valid"
                )
            )
        }
    }

    @GetMapping("/roles")
    @PreAuthorize("isAuthenticated()")
    fun getCurrentUserRoles(): ResponseEntity<*> {
        logger.debug("Retrieving current user roles")

        return try {
            val keycloakId = identityFacade.getCurrentKeycloakId()
            if (keycloakId != null) {
                val roles = identityFacade.getUserRoles(keycloakId)
                ResponseEntity.ok(
                    UserRolesResponse(
                        keycloakId = keycloakId,
                        roles = roles
                    )
                )
            } else {
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ErrorResponse(
                        error = "INVALID_AUTHENTICATION",
                        message = "Invalid authentication",
                        details = "Please log in again"
                    )
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to retrieve user roles", e)
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "ROLES_RETRIEVAL_FAILED",
                    message = e.message ?: "Failed to retrieve roles",
                    details = "Please ensure you are properly authenticated"
                )
            )
        }
    }

    @GetMapping("/permissions/{permission}")
    @PreAuthorize("isAuthenticated()")
    fun checkPermission(
        @PathVariable permission: String,
        @RequestParam(required = false) businessId: UUID?
    ): ResponseEntity<*> {
        logger.debug("Checking permission: $permission for business: $businessId")

        return try {
            val keycloakId = identityFacade.getCurrentKeycloakId()
            if (keycloakId != null) {
                val permissionEnum = com.dominikdev.booking.identity.domain.Permission.valueOf(permission)
                val hasPermission = identityFacade.hasPermission(keycloakId, permissionEnum, businessId)

                ResponseEntity.ok(
                    PermissionCheckResponse(
                        permission = permission,
                        businessId = businessId,
                        hasPermission = hasPermission
                    )
                )
            } else {
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ErrorResponse(
                        error = "INVALID_AUTHENTICATION",
                        message = "Invalid authentication",
                        details = "Please log in again"
                    )
                )
            }
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "INVALID_PERMISSION",
                    message = "Invalid permission: $permission",
                    details = "Please provide a valid permission name"
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to check permission: $permission", e)
            ResponseEntity.badRequest().body(
                ErrorResponse(
                    error = "PERMISSION_CHECK_FAILED",
                    message = e.message ?: "Failed to check permission",
                    details = "Please try again"
                )
            )
        }
    }

    // Health Check Endpoint
    @GetMapping("/health")
    fun healthCheck(): ResponseEntity<*> {
        return ResponseEntity.ok(
            mapOf(
                "status" to "UP",
                "service" to "Identity Service",
                "timestamp" to System.currentTimeMillis()
            )
        )
    }
}

// Response DTOs
data class ErrorResponse(
    val error: String,
    val message: String,
    val details: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class SuccessResponse(
    val message: String,
    val details: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class BusinessUsersResponse(
    val businessId: UUID,
    val users: List<UserAccount>,
    val totalCount: Int
)

data class UserRolesResponse(
    val keycloakId: String,
    val roles: List<UserRole>
)

data class PermissionCheckResponse(
    val permission: String,
    val businessId: UUID?,
    val hasPermission: Boolean
)
