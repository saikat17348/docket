package com.azentio.docket.config;

import com.azentio.docket.entity.AppUser;
import com.azentio.docket.enums.UserRole;
import com.azentio.docket.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedUsers() {
        return args -> {
            createUser("analyst1", "analyst1@docket.com", UserRole.ANALYST);
            createUser("analyst2", "analyst2@docket.com", UserRole.ANALYST);
            createUser("supervisor1", "supervisor1@docket.com", UserRole.SUPERVISOR);
        };
    }

    private void createUser(String username, String email, UserRole role) {

        if (appUserRepository.findByUsername(username).isPresent()) {
            return;
        }

        AppUser user = AppUser.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode("Password@123"))
                .role(role)
                .active(true)
                .createdAt(Instant.now().atZone(java.time.ZoneOffset.UTC).toLocalDateTime())
                .build();

        appUserRepository.save(user);
    }
}