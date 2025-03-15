package com.dominikdev.booking.infrastructure.config

import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KeycloakConfig {

    @Value("\${keycloak.auth-server-url}")
    private lateinit var authServerUrl: String

    private fun getServerUrl(): String {
        // Remove trailing slash if present
        val baseUrl = authServerUrl.trimEnd('/')
        // For Keycloak 17+ running in Docker with start-dev mode
        return baseUrl
    }

    @Value("\${keycloak.realm}")
    private lateinit var realm: String

    @Value("\${keycloak.admin-realm:master}")
    private lateinit var adminRealm: String

    @Value("\${keycloak.admin-username}")
    private lateinit var adminUsername: String

    @Value("\${keycloak.admin-password}")
    private lateinit var adminPassword: String

    @Value("\${keycloak.resource}")
    private lateinit var clientId: String

    @Value("\${keycloak.credentials.secret}")
    private lateinit var clientSecret: String

    @Bean
    fun keycloak(): Keycloak {
        return KeycloakBuilder.builder()
            .serverUrl(getServerUrl())
            .realm(adminRealm)
            .username(adminUsername)
            .password(adminPassword)
            .clientId("admin-cli")
            .build()
    }
}