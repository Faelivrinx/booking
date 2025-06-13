package com.dominikdev.booking.identity.infrastructure

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfiguration(
    @Value("\${keycloak.auth-server-url}/realms/\${keycloak.realm}/protocol/openid-connect/certs")
    private val jwkSetUri: String
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .authorizeHttpRequests { authorize ->
                authorize
                    // Public endpoints
                    .requestMatchers("/api/identity/clients/register").permitAll()
                    .requestMatchers("/api/identity/password-reset").permitAll()
                    .requestMatchers("/actuator/**").permitAll()

                    // Admin only endpoints
                    .requestMatchers("/api/identity/business-owners").hasRole("ADMIN")

                    // Business owner endpoints
                    .requestMatchers("/api/identity/employees").hasRole("BUSINESS_OWNER")
                    .requestMatchers("/api/identity/employees/*/deactivate").hasRole("BUSINESS_OWNER")

                    // Authenticated user endpoints
                    .requestMatchers("/api/identity/profile").authenticated()
                    .requestMatchers("/api/identity/users/*").hasAnyRole("BUSINESS_OWNER", "ADMIN")

                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .build()
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build()
    }

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val jwtAuthenticationConverter = JwtAuthenticationConverter()
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(KeycloakRoleConverter())
        return jwtAuthenticationConverter
    }


    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("*")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("Authorization", "Content-Type")
        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    class KeycloakRoleConverter : Converter<Jwt, Collection<GrantedAuthority>> {
        private val jwtGrantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()

        override fun convert(jwt: Jwt): Collection<GrantedAuthority> {
            val grantedAuthorities = jwtGrantedAuthoritiesConverter.convert(jwt)?.toMutableSet() ?: mutableSetOf()

            // Extract realm roles
            val realmAccess = jwt.claims["realm_access"] as? Map<*, *>
            val realmRoles = realmAccess?.get("roles") as? List<*>

            if (realmRoles != null) {
                val roles = realmRoles
                    .filterIsInstance<String>()
                    .map { role -> SimpleGrantedAuthority("ROLE_$role") }

                grantedAuthorities.addAll(roles)
            }

            // Extract resource roles if needed
            val resourceAccess = jwt.claims["resource_access"] as? Map<*, *>
            resourceAccess?.forEach { (resource, access) ->
                if (access is Map<*, *> && access.containsKey("roles")) {
                    val roles = access["roles"] as? List<*>
                    roles?.filterIsInstance<String>()
                        ?.forEach { role ->
                            grantedAuthorities.add(SimpleGrantedAuthority("ROLE_${resource}_$role"))
                        }
                }
            }

            return grantedAuthorities
        }
    }
}