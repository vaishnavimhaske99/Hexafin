
package com.example.demo.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.demo.security.JwtAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // CORS Configuration
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
            Arrays.asList("http://localhost:4200")
        );

        configuration.setAllowedMethods(
            Arrays.asList(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
            )
        );

        configuration.setAllowedHeaders(
            Arrays.asList("*")
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .cors(cors ->
                cors.configurationSource(corsConfigurationSource())
            )

            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())

            .authorizeHttpRequests(auth -> auth

            	    .requestMatchers(
            	        HttpMethod.POST,
            	        "/api/auth/login",
            	        "/api/auth/signup"
            	    ).permitAll()

            	    .requestMatchers(
            	        HttpMethod.GET,
            	        "/api/leads",
            	        "/api/leads/",
            	        "/api/leads/**"
            	    ).permitAll()
            	    .requestMatchers(
            	    	    HttpMethod.GET,
            	    	    "/api/leads/*"
            	    	).permitAll()
            	    .requestMatchers(
            	        HttpMethod.POST,
            	        "/api/leads",
            	        "/api/leads/"
            	    ).permitAll()

            	    .requestMatchers(
            	        HttpMethod.POST,
            	        "/api/leads/import",
            	        "/api/leads/import/"
            	    ).permitAll()

            	    .requestMatchers(
            	        HttpMethod.GET,
            	        "/api/dashboard/**"
            	    ).permitAll()

            	    // Temporary: Dashboard Sales Users table
            	    .requestMatchers(
            	        HttpMethod.GET,
            	        "/api/users",
            	        "/api/users/"
            	    ).permitAll()

            	    // Other user endpoints remain ADMIN-only
            	    .requestMatchers("/api/users/**").hasRole("ADMIN")

            	    .requestMatchers("/api/**").authenticated()

            	    .anyRequest().permitAll()
            	)
            	.addFilterBefore(
            	    jwtAuthenticationFilter,
            	    UsernamePasswordAuthenticationFilter.class
            	);
        // Re-enable it when JWT authentication is restored.
       
        return http.build();
    }
}
