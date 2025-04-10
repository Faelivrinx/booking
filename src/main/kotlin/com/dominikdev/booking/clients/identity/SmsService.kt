package com.dominikdev.booking.clients.identity

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class SmsService {
    private val logger = KotlinLogging.logger {}

    fun sendVerificationSms(phoneNumber: String, verificationCode: String): Boolean {
        // In a real application, this would send an SMS via a service like Twilio
        logger.info { "MOCK SMS NOTIFICATION: Sending verification code $verificationCode to $phoneNumber" }
        return true
    }
}