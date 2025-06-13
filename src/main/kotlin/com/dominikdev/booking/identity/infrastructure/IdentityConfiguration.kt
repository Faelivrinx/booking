package com.dominikdev.booking.identity.infrastructure

import com.dominikdev.booking.identity.domain.IdentityProvider
import com.dominikdev.booking.identity.domain.UserProfileRepository
import org.keycloak.admin.client.Keycloak
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class IdentityConfiguration(

) {
    @Bean
    fun identityProvider(keycloak: Keycloak): IdentityProvider {
        return DefaultIdentityProvider(keycloak, "appointment-realm")
    }

    @Bean
    fun userProfileRepository(jpaRepository: UserProfileJpaRepository): UserProfileRepository {
        return UserProfileRepositoryImpl(jpaRepository)
    }
}