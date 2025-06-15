package com.dominikdev.booking.identity.infrastructure

import com.dominikdev.booking.identity.IdentityFacade
import com.dominikdev.booking.identity.application.DefaultIdentityFacade
import com.dominikdev.booking.identity.application.IdentityApplicationService
import com.dominikdev.booking.identity.domain.IdentityProvider
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class IdentityConfiguration {
    private val logger = LoggerFactory.getLogger(IdentityConfiguration::class.java)

    @Bean
    @Primary
    fun keycloak(
        @Value("\${keycloak.auth-server-url}") authServerUrl: String,
        @Value("\${keycloak.admin-username}") adminUsername: String,
        @Value("\${keycloak.admin-password}") adminPassword: String,
        @Value("\${keycloak.admin-realm:master}") adminRealm: String
    ): Keycloak {
        logger.info("Configuring Keycloak admin client for realm: $adminRealm")

        return KeycloakBuilder.builder()
            .serverUrl(authServerUrl)
            .realm(adminRealm)
            .username(adminUsername)
            .password(adminPassword)
            .clientId("admin-cli")
            .build().also {
                logger.info("Keycloak admin client configured successfully")
            }
    }

    @Bean
    fun identityProvider(
        keycloak: Keycloak,
        @Value("\${keycloak.realm}") realm: String
    ): IdentityProvider {
        logger.info("Configuring IdentityProvider for realm: $realm")
        return DefaultIdentityProvider(keycloak, realm)
    }

    @Bean
    fun identityApplicationService(identityProvider: IdentityProvider): IdentityApplicationService {
        logger.info("Configuring IdentityApplicationService")
        return IdentityApplicationService(identityProvider)
    }

    @Bean
    fun identityFacade(
        identityApplicationService: IdentityApplicationService,
        identityProvider: IdentityProvider
    ): IdentityFacade {
        logger.info("Configuring IdentityFacade")
        return DefaultIdentityFacade(identityApplicationService, identityProvider)
    }
}