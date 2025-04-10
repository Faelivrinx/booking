package com.dominikdev.booking.shared.infrastructure.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
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
                    .requestMatchers("/clients/register", "/clients/activate", "/clients/resend-code").permitAll()
                    // Alternative business path (if using context path properly)
                    // actuator should be public
                    .requestMatchers("/actuator/**").permitAll()
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