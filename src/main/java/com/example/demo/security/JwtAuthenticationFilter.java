package com.example.demo.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("\n========== JWT FILTER START ==========");
        System.out.println("REQUEST METHOD: " + request.getMethod());
        System.out.println("REQUEST URI: " + request.getRequestURI());

        String authHeader = request.getHeader("Authorization");

        boolean hasBearerToken =
                authHeader != null && authHeader.startsWith("Bearer ");

        System.out.println("AUTH HEADER PRESENT: " + hasBearerToken);

        if (!hasBearerToken) {
            System.out.println("JWT: No Bearer token supplied");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();

        try {
            String email = jwtService.extractEmail(token);

            System.out.println("JWT EMAIL: " + email);

            if (email == null || email.isBlank()) {
                System.out.println("JWT ERROR: Email claim is missing");
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            if (SecurityContextHolder.getContext()
                    .getAuthentication() == null) {

                User user = userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found for email: " + email));

                System.out.println("JWT USER ID: " + user.getId());
                System.out.println("JWT USER EMAIL: " + user.getEmail());
                System.out.println("JWT USER ROLE: " + user.getRole());

                boolean valid =
                        jwtService.isTokenValid(token, user.getEmail());

                System.out.println("JWT VALID: " + valid);

                if (!valid) {
                    System.out.println("JWT ERROR: Token invalid or expired");
                    SecurityContextHolder.clearContext();
                } else if (user.getRole() == null) {
                    System.out.println("JWT ERROR: User role is null");
                    SecurityContextHolder.clearContext();
                } else {
                    String roleAuthority = "ROLE_" + user.getRole().name();

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    Collections.singletonList(
                                            new SimpleGrantedAuthority(roleAuthority)
                                    )
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);

                    System.out.println("AUTHENTICATION SET: "
                            + authentication.getName());
                    System.out.println("AUTH USER ROLE: " + user.getRole());
                    System.out.println("AUTH AUTHORITIES: "
                            + authentication.getAuthorities());
                    System.out.println("IS AUTHENTICATED: "
                            + authentication.isAuthenticated());
                }

            } else {
                System.out.println("AUTHENTICATION ALREADY EXISTS: "
                        + SecurityContextHolder.getContext()
                                .getAuthentication().getAuthorities());
            }

        } catch (Exception e) {
            System.out.println("JWT ERROR TYPE: "
                    + e.getClass().getSimpleName());
            System.out.println("JWT ERROR MESSAGE: " + e.getMessage());

            SecurityContextHolder.clearContext();
        }

        System.out.println("========== JWT FILTER END ==========\n");

        filterChain.doFilter(request, response);
    }
}
