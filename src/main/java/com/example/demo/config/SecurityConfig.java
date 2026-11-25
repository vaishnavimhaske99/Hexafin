package com.example.demo.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.demo.security.JwtAuthenticationFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)  // 🔥 prePostEnabled
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // CORS Configuration
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
        	    "http://localhost:4200",
        	    "https://crm.hexafin.com"
        	));
        configuration.setAllowedMethods(
                Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())

            .authorizeHttpRequests(auth -> auth

                // 🔓 PUBLIC — Login/Signup
                .requestMatchers(HttpMethod.POST,
                        "/api/auth/login",
                        "/api/auth/signup"
                ).permitAll()

                // 🔓 PUBLIC — Leads (GET + POST) for development
                .requestMatchers(HttpMethod.GET, "/api/leads", "/api/leads/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/leads", "/api/leads/import").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/dashboard/**").permitAll()

                // 🔐 Own profile (any logged-in user)
                .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()

                // 🔐 Manager team endpoint
                .requestMatchers(HttpMethod.GET, "/api/users/team").hasRole("MANAGER")

                // 🔐 Hierarchy update — ADMIN only
                .requestMatchers(HttpMethod.PUT, "/api/users/*/hierarchy").hasRole("ADMIN")

                // 🔐 Users list — ADMIN, MANAGER, TEAM_LEAD
                .requestMatchers(HttpMethod.GET, "/api/users", "/api/users/")
                    .hasAnyRole("ADMIN", "MANAGER", "TEAM_LEAD")

                // 🔐 Users/{id} — ADMIN, MANAGER, TEAM_LEAD
                .requestMatchers(HttpMethod.GET, "/api/users/*")
                    .hasAnyRole("ADMIN", "MANAGER", "TEAM_LEAD")

                // 🔐 Sales User चे Leads — ADMIN, MANAGER, TEAM_LEAD
                .requestMatchers(HttpMethod.GET, "/api/leads/sales-user/*")
                    .hasAnyRole("ADMIN", "MANAGER", "TEAM_LEAD")

                // 🔐 Lead Assign — ADMIN, MANAGER, TEAM_LEAD
                .requestMatchers(HttpMethod.PUT, "/api/leads/*/assign/*")
                    .hasAnyRole("ADMIN", "MANAGER", "TEAM_LEAD")

                // 🔐 बाकी सगळं — authenticated
                .requestMatchers("/api/**").authenticated()
                .requestMatchers("/api/locations/**").authenticated()
                .requestMatchers("/api/notifications/**").authenticated()
                .anyRequest().permitAll()
            )

            .addFilterBefore(
                    jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}