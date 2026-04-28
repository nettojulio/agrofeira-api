package br.edu.ufape.agrofeira.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.servlet.HandlerExceptionResolver

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    @Qualifier("handlerExceptionResolver") private val exceptionResolver: HandlerExceptionResolver,
) {
    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtAuthFilterProvider: org.springframework.beans.factory.ObjectProvider<JwtAuthFilter>,
    ): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { }
            .headers { headers ->
                headers.frameOptions { it.deny() }
                headers.contentSecurityPolicy { it.policyDirectives("default-src 'self'; script-src 'self'; object-src 'none';") }
                headers.xssProtection { it.disable() } // Modern browsers handle this, or use CSP
                headers.httpStrictTransportSecurity { it.includeSubDomains(true).maxAgeInSeconds(31536000) }
            }.sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }.authorizeHttpRequests {
                it
                    .requestMatchers(
                        "/api/v1/auth/login",
                        "/api/v1/auth/refresh",
                        "/api/v1/auth/forgot-password",
                        "/api/v1/auth/reset-password",
                    ).permitAll()
                it.requestMatchers("/api/v1/auth/logout", "/api/v1/auth/register").authenticated()
                it.requestMatchers("/actuator/**").permitAll()
                it.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                it.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/feiras/**").permitAll()
                it.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/zonas-entrega/**").permitAll()
                it.anyRequest().authenticated()
            }.exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint { request, response, exception ->
                        exceptionResolver.resolveException(request, response, null, exception)
                    }.accessDeniedHandler { request, response, exception ->
                        exceptionResolver.resolveException(request, response, null, exception)
                    }
            }.addFilterBefore(jwtAuthFilterProvider.ifAvailable!!, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager = config.authenticationManager
}
