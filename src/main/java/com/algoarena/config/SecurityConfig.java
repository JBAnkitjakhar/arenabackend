// src/main/java/com/algoarena/config/SecurityConfig.java
package com.algoarena.config;

import com.algoarena.security.JwtAuthenticationFilter;
import com.algoarena.security.OAuth2SuccessHandler;
import com.algoarena.security.OAuth2FailureHandler;
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
    private OAuth2FailureHandler oAuth2FailureHandler;

    @Autowired
    private AppConfig appConfig;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // PUBLIC ENDPOINTS (No authentication required)
                        .requestMatchers(
                                "/auth/**",
                                "/oauth2/**",
                                "/login/**",
                                "/health",

                                // KEEP-ALIVE STATUS ENDPOINTS (CRITICAL FOR RENDER)
                                "/status",       
                                "/ping",
                                "/healthz", 

                                "/actuator/**",
                                "/error"
                        ).permitAll()
                        
                        // AUTHENTICATED USER ENDPOINTS - READ ACCESS
                        .requestMatchers(HttpMethod.GET,
                                "/questions/summary",              // Questions with user progress
                                "/questions",                      // Questions list (with filters)
                                "/questions/{id}",                 // Question details
                                "/categories/with-progress",       // Categories with user progress
                                "/categories",                     // Basic categories list
                                "/categories/{id}",                // Category details
                                "/categories/{id}/stats",          // Category statistics
                                "/categories/{id}/progress",       // User progress for category
                                "/solutions/question/*",           // View solutions by question
                                "/solutions/{id}",                 // View individual solutions
                                "/approaches/**",                  // User approaches (all operations)
                                "/compiler/**",                    // Code execution
                                "/users/progress",                 // User progress stats
                                "/users/progress/recent",          // Recent user progress
                                "/files/solutions/*/visualizers", // List visualizers by solution
                                "/files/visualizers/**"           // Access visualizer files
                        ).authenticated()
                        
                        // USER PROGRESS UPDATE ENDPOINTS
                        .requestMatchers(HttpMethod.PUT,
                                "/questions/*/progress"            // Update question progress (mark solved/unsolved)
                        ).authenticated()
                        
                        .requestMatchers(HttpMethod.POST,
                                "/questions/*/progress"            // Create/update question progress
                        ).authenticated()
                        
                        // USER APPROACH MANAGEMENT ENDPOINTS
                        .requestMatchers(HttpMethod.POST, "/approaches/question/*").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/approaches/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/approaches/*").authenticated()
                        
                        // ADMIN-ONLY ENDPOINTS - CREATE/UPDATE/DELETE OPERATIONS
                        .requestMatchers(
                                "/admin/**",                       // All admin endpoints
                                "/questions/stats",                // Question statistics
                                "/questions/search",               // Question search (admin)
                                "/solutions/question/*/create",    // Create solutions
                                "/solutions/*/update",             // Update solutions
                                "/solutions/*/delete",             // Delete solutions
                                "/files/images/**",                // Image uploads (admin only)
                                "/files/visualizers/*/upload",    // Visualizer uploads (admin only)
                                "/files/visualizers/*/delete",    // Visualizer deletes (admin only)
                                "/files/visualizers/*/metadata",  // Visualizer metadata (admin only)
                                "/files/visualizers/*/download"   // Visualizer downloads (admin only)
                        ).hasAnyRole("ADMIN", "SUPERADMIN")
                        
                        // ADMIN CREATE/UPDATE/DELETE OPERATIONS
                        .requestMatchers(HttpMethod.POST, "/questions", "/categories", "/solutions").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers(HttpMethod.PUT, "/questions/*", "/categories/*", "/solutions/*").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/questions/*", "/categories/*", "/solutions/*").hasAnyRole("ADMIN", "SUPERADMIN")
                        
                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                // Add exception handling
                .exceptionHandling(exceptions -> exceptions
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(401);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"Authentication required\",\"message\":\"Please provide valid JWT token\"}");
                    })
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setStatus(403);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"Access denied\",\"message\":\"You don't have permission to access this resource\"}");
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
                        .failureHandler(oAuth2FailureHandler)
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