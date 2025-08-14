// src/main/java/com/algoarena/config/SecurityConfig.java
package com.algoarena.config;

import com.algoarena.security.JwtAuthenticationFilter;
import com.algoarena.security.OAuth2SuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @Autowired
    private AppConfig appConfig;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ✅ FIXED: Remove context path since Spring Security matches AFTER context path resolution
                        // Public endpoints - Spring Security sees these WITHOUT /api prefix
                        .requestMatchers(
                                "/auth/**",           // Matches /api/auth/** 
                                "/oauth2/**",         // Matches /api/oauth2/**
                                "/login/**",          // Matches /api/login/**
                                "/health",            // Matches /api/health
                                "/actuator/**",       // Matches /api/actuator/**
                                "/error"              // Matches /api/error
                        ).permitAll()
                        
                        // Admin only endpoints
                        .requestMatchers(
                                "/admin/**",          // Matches /api/admin/**
                                "/dsa/admin/**"       // Matches /api/dsa/admin/**
                        ).hasAnyRole("ADMIN", "SUPERADMIN")
                        
                        // All other requests need authentication
                        .anyRequest().authenticated()
                )
                // OAuth2 Login Configuration
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/oauth2/authorization")
                        )
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/oauth2/callback/*")
                        )
                        .successHandler(oAuth2SuccessHandler)
                        .failureUrl("http://localhost:3000/auth/login?error=oauth_failed")
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Get allowed origins from config - handle potential null/empty values
        String origins = appConfig.getCors().getAllowedOrigins();
        if (origins != null && !origins.trim().isEmpty()) {
            List<String> allowedOrigins = Arrays.asList(origins.split(","));
            configuration.setAllowedOriginPatterns(allowedOrigins);
        } else {
            // Default fallback
            configuration.setAllowedOriginPatterns(List.of("http://localhost:3000"));
        }
        
        // Handle methods
        String methods = appConfig.getCors().getAllowedMethods();
        if (methods != null && !methods.trim().isEmpty()) {
            configuration.setAllowedMethods(Arrays.asList(methods.split(",")));
        } else {
            // Default methods
            configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        }
        
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(appConfig.getCors().isAllowCredentials());
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @SuppressWarnings("deprecation")
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}