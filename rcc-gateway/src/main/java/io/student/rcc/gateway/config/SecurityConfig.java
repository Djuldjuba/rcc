package io.student.rcc.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${rococo-front.base-uri:http://localhost:3000}")
    private String frontUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Публичные GET для всех сущностей
                        .requestMatchers(HttpMethod.GET, "/api/session").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/artist/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/museum/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/painting/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/country/**").permitAll()

                        // Все /api/user/** требуют аутентификации
                        .requestMatchers("/api/user/**").authenticated()

                        // POST, PUT, DELETE - требуют аутентификации
                        .requestMatchers(HttpMethod.POST, "/api/artist/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/artist/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/artist/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/museum/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/museum/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/museum/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/painting/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/painting/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/painting/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/country/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/country/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/country/**").authenticated()

                        // Все остальное - требует аутентификации
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(new JwtAuthenticationConverter())
                        )
                );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of(frontUri, "http://localhost:3000"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}