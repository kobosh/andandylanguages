package com.nubianlanguages.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // 🔴 FORCE this chain to apply to ALL requests
                .securityMatcher("/**")
                .cors(Customizer.withDefaults())

                // 🔴 CSRF MUST be disabled for POST APIs
                .csrf(csrf -> csrf.disable())

                // 🔴 NO sessions, NO login pages
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // 🔴 Explicit authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/.well-known/jwks.json",
                                "/h2-console/**"
                        ).permitAll()
                        .anyRequest().denyAll()
                )

                // 🔴 Required for H2 console
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
