package com.skillbridge.config;

import com.skillbridge.model.User;
import com.skillbridge.model.enums.Role;
import com.skillbridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
@EnableMongoAuditing
@EnableAsync
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class AppConfig implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    /**
     * ApplicationRunner runs AFTER the full Spring context is ready
     * including MongoDB connection – safer than CommandLineRunner.
     */
    @Override
    public void run(ApplicationArguments args) {
        try {
            if (!userRepository.existsByEmail(adminEmail)) {
                User admin = new User();
                admin.setName("SkillBridge Admin");
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRole(Role.ROLE_ADMIN);
                admin.setActive(true);
                admin.setCreatedAt(LocalDateTime.now());

                userRepository.save(admin);
                log.info("✅ Admin account seeded: {}", adminEmail);
            } else {
                log.info("ℹ️ Admin account already exists: {}", adminEmail);
            }
        } catch (Exception e) {
            log.error("❌ Failed to seed admin: {}", e.getMessage(), e);
        }
    }
}