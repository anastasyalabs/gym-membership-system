//package com.gym.gym_membership_system.security;
//
//import com.gym.gym_membership_system.service.UserAccountService;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//@Component
//public class JwtAuthFilter extends OncePerRequestFilter {
//
//    private final JwtUtil jwtUtil;
//    private final UserAccountService userAccountService;
//
//    public JwtAuthFilter(JwtUtil jwtUtil, UserAccountService userAccountService) {
//        this.jwtUtil = jwtUtil;
//        this.userAccountService = userAccountService;
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain)
//            throws ServletException, IOException {
//
//        // 1. Get Authorization header
//        String authHeader = request.getHeader("Authorization");
//
//        // 2. Check it starts with "Bearer "
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        // 3. Extract the token
//        String token = authHeader.substring(7);
//
//        // 4. Validate and set authentication
//        if (jwtUtil.validateToken(token)) {
//            String email = jwtUtil.extractEmail(token);
//
//            UserDetails userDetails = userAccountService.loadUserByUsername(email);
//
//            UsernamePasswordAuthenticationToken authentication =
//                    new UsernamePasswordAuthenticationToken(
//                            userDetails,
//                            null,
//                            userDetails.getAuthorities()
//                    );
//            authentication.setDetails(
//                    new WebAuthenticationDetailsSource().buildDetails(request)
//            );
//
//            // 5. Tell Spring Security this request is authenticated
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//        }
//
//        filterChain.doFilter(request, response);
//    }
//}