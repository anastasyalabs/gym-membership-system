//package com.gym.gym_membership_system.security;
//
//import com.gym.gym_membership_system.service.UserAccountService;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    private final JwtAuthFilter jwtAuthFilter;
//    private final UserAccountService userAccountService;
//    private final PasswordEncoder passwordEncoder;
//
//    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
//                          UserAccountService userAccountService,
//                          PasswordEncoder passwordEncoder) {
//        this.jwtAuthFilter = jwtAuthFilter;
//        this.userAccountService = userAccountService;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                // Disable CSRF — not needed for REST APIs with JWT
//                .csrf(csrf -> csrf.disable())
//
//                // Allow H2 console to render in frames
//                .headers(headers -> headers
//                        .frameOptions(frame -> frame.sameOrigin()))
//
//                // Define which endpoints are public and which require auth
//                .authorizeHttpRequests(auth -> auth
//
//                        // ── Public endpoints (no login required) ──────────────────
//                        .requestMatchers("/api/public/**").permitAll()
//                        .requestMatchers("/h2-console/**").permitAll()
//
//                        // ── Member endpoints ──────────────────────────────────────
//                        .requestMatchers("/api/member/**").hasRole("MEMBER")
//
//                        // ── Admin endpoints ───────────────────────────────────────
//                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
//
//                        // ── Everything else requires authentication ────────────────
//                        .anyRequest().authenticated()
//                )
//
//                // Use stateless sessions — JWT handles auth, no server session needed
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//
//                // Register our JWT filter before Spring's default login filter
//                .authenticationProvider(authenticationProvider())
//                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//    @Bean
//    public AuthenticationProvider authenticationProvider() {
//        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//        provider.setUserDetailsService(userAccountService);
//        provider.setPasswordEncoder(passwordEncoder);
//        return provider;
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(
//            AuthenticationConfiguration config) throws Exception {
//        return config.getAuthenticationManager();
//    }
//}