package io.student.rcc.geo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Все GET для country - публичные
                        .requestMatchers(HttpMethod.GET, "/api/country/**").permitAll()
                        // POST, PUT, DELETE - требуют аутентификации (если будут)
                        .requestMatchers(HttpMethod.POST, "/api/country/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/country/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/country/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/country/**").authenticated()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(new JwtAuthenticationConverter())
                        )
                );

        return http.build();
    }
}