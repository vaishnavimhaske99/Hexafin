
package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Configuration
public class AdminPasswordUpdater {

    @Bean
    CommandLineRunner updateAdminPassword(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            User admin = userRepository.findById(1L)
                    .orElseThrow(() ->
                            new RuntimeException("Admin not found"));

            if (!"ADMIN".equals(admin.getRole().name())) {
                throw new RuntimeException("User is not ADMIN");
            }

            String newPassword = System.getenv("ADMIN_NEW_PASSWORD");

            if (newPassword == null || newPassword.isBlank()) {
                System.out.println("ADMIN PASSWORD UPDATE SKIPPED");
                return;
            }

            admin.setPassword(passwordEncoder.encode(newPassword));

            userRepository.save(admin);

            System.out.println("ADMIN PASSWORD UPDATED SUCCESSFULLY");
        };
    }
}
