package com.adabyron.infraestructure.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Usamos nuestra CorsConfig en lugar de la configuración por defecto de Security
            .cors(cors -> cors.configurationSource(corsConfigurationSource))

            // CSRF desactivado: Las  peticiones vienen de Next.js con credentials:include,
            // no de formularios HTML clásicos, por lo que podemos desactivarlo en este contexto
            .csrf(csrf -> csrf.disable())

            // Le decimos a Spring Security que se encargue el de gestionar las sesiones,
           .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation().migrateSession()   
                .maximumSessions(1)                  
            )

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/login",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/api-docs",
                    "/api-docs/**"
                ).permitAll()  // login siempre accesible
                .anyRequest().permitAll()                        
            );

        return http.build();
    }
}