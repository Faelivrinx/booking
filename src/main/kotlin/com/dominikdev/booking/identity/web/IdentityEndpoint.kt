package com.dominikdev.booking.identity.web

import com.dominikdev.booking.identity.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/identity")
class IdentityEndpoint(
    private val identityFacade: IdentityFacade,
) {

    @PostMapping("/clients/register")
    fun registerClient(@RequestBody request: ClientRegistrationRequest): ResponseEntity<*> {
        return try {
            val result = identityFacade.registerClient(request)
            ResponseEntity.status(HttpStatus.CREATED).body(result)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Registration failed")))
        }
    }

    @PostMapping("/business-owners")
    @PreAuthorize("hasRole('ADMIN')") // Only admins can create business owners
    fun createBusinessOwner(@RequestBody request: CreateBusinessOwnerRequest): ResponseEntity<*> {
        return try {
            val userAccount = identityFacade.createBusinessOwner(request)
            ResponseEntity.status(HttpStatus.CREATED).body(userAccount)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Failed to create business owner")))
        }
    }

    @PostMapping("/employees")
    @PreAuthorize("hasRole('BUSINESS_OWNER')") // Only business owners can create employees
    fun createEmployee(@RequestBody request: CreateEmployeeAccountRequest): ResponseEntity<*> {
        return try {
            val userAccount = identityFacade.createEmployeeAccount(request)
            ResponseEntity.status(HttpStatus.CREATED).body(userAccount)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Failed to create employee")))
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    fun getCurrentUserProfile(): ResponseEntity<*> {
        return try {
            val userProfile = identityFacade.getCurrentUserProfile()
            if (userProfile != null) {
                ResponseEntity.ok(userProfile)
            } else {
                ResponseEntity.notFound().build<Any>()
            }
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Failed to get profile")))
        }
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    fun updateProfile(
        @RequestBody request: UpdateProfileRequest,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<*> {
        return try {
            val userProfile = identityFacade.getCurrentUserProfile()
            if (userProfile != null) {
                val updatedAccount = identityFacade.updateProfile(userProfile.id, request)
                ResponseEntity.ok(updatedAccount)
            } else {
                ResponseEntity.notFound().build<Any>()
            }
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Failed to update profile")))
        }
    }

    @PostMapping("/password-reset")
    fun requestPasswordReset(@RequestBody request: PasswordResetRequest): ResponseEntity<*> {
        return try {
            identityFacade.requestPasswordReset(request.email)
            ResponseEntity.ok(mapOf("message" to "Password reset email sent if account exists"))
        } catch (e: Exception) {
            // Don't reveal if email exists for security
            ResponseEntity.ok(mapOf("message" to "Password reset email sent if account exists"))
        }
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('ADMIN')")
    fun getUserAccount(@PathVariable userId: UUID): ResponseEntity<*> {
        return try {
            val userAccount = identityFacade.getClientAccount(userId)
                ?: identityFacade.getEmployeeAccount(userId)
                ?: identityFacade.getBusinessOwner(userId)

            if (userAccount != null) {
                ResponseEntity.ok(userAccount)
            } else {
                ResponseEntity.notFound().build<Any>()
            }
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Failed to get user")))
        }
    }

    @PostMapping("/employees/{userId}/deactivate")
    @PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('ADMIN')")
    fun deactivateEmployee(@PathVariable userId: UUID): ResponseEntity<*> {
        return try {
            identityFacade.deactivateEmployeeAccount(userId)
            ResponseEntity.ok(mapOf("message" to "Employee account deactivated"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Failed to deactivate employee")))
        }
    }
}
