package com.dominikdev.booking.business.staff

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class StaffNotificationService {
    private val logger = KotlinLogging.logger {}

    /**
     * Sends an invitation email to a new staff member
     */
    fun sendInvitation(email: String, name: String, temporaryPassword: String, businessName: String): Boolean {
        // In a real application, this would use an email service
        // For now, we'll just log the invitation

        logger.info {
            """
            MOCK EMAIL NOTIFICATION: 
            To: $email
            Subject: You've been invited to join $businessName as a staff member
            
            Dear $name,
            
            You have been invited to join $businessName as a staff member.
            
            To get started, please log in to the system using the following credentials:
            Email: $email
            Temporary Password: $temporaryPassword
            
            You will be asked to change your password on first login.
            
            Best regards,
            The $businessName Team
            """
        }

        return true
    }
}