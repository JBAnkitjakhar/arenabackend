// src/main/java/com/algoarena/config/SecurityConfig.java
// need update in prod at line 101, 119, 142
package com.algoarena.config;

import com.algoarena.security.JwtAuthenticationFilter;
import com.algoarena.security.OAuth2SuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                    // ✅ PUBLIC ENDPOINTS (No authentication required)
                    .requestMatchers(
                            "/auth/**",
                            "/oauth2/**",
                            "/login/**",
                            "/health",
                            "/actuator/**",
                            "/error"
                    ).permitAll()
                    
                    // ✅ VISUALIZER ACCESS (Authenticated users only) - MOVE THIS UP!
                    .requestMatchers(
                            HttpMethod.GET,
                            "/files/visualizers/*"  // Fixed: GET access to visualizer files
                    ).authenticated()
                    
                    // ✅ ADMIN-ONLY ENDPOINTS 
                    .requestMatchers(
                            "/admin/**",
                            "/categories/**",
                            "/questions/**",
                            "/solutions/question/*/create",
                            "/solutions/*/update",
                            "/solutions/*/delete",
                            "/files/images/**",            // Image uploads (admin only)
                            "/files/visualizers/*/upload", // Visualizer uploads (admin only) 
                            "/files/visualizers/*/delete"  // Visualizer deletes (admin only)
                    ).hasAnyRole("ADMIN", "SUPERADMIN")
                    
                    // ✅ OTHER AUTHENTICATED ENDPOINTS
                    .requestMatchers(
                            "/questions/*/view",
                            "/solutions/question/*",
                            "/solutions/*",
                            "/approaches/**",
                            "/compiler/**",
                            "/users/**",
                            "/files/solutions/*/visualizers"
                    ).authenticated()
                    
                    .requestMatchers("/questions").permitAll()
                    
                    // Everything else requires authentication
                    .anyRequest().authenticated()
            )
            // Add exception handling to prevent redirects
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.getWriter().write("Authentication required");
                })
            )
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
        return new BCryptPasswordEncoder(); // i think i dont need this , becuase we dont use email andpassword logins
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