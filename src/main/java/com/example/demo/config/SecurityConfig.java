package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for OAuth2 client-only setup.
 *
 * <p>Why this is needed: - spring-boot-starter-security-oauth2-client pulls in Spring Security -
 * Spring Security by default protects ALL endpoints (redirects to /login) - But we only need OAuth2
 * for OUTGOING RestClient calls, not endpoint protection
 *
 * <p>This config permits all endpoints while keeping OAuth2 client beans
 * (OAuth2AuthorizedClientManager, etc.) available for outgoing HTTP calls.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
