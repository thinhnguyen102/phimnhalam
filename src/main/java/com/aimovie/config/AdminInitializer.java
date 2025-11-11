package com.aimovie.config;

import com.aimovie.entity.Role;
import com.aimovie.entity.User;
import com.aimovie.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class AdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        userRepository.findByUsername("admin").ifPresentOrElse(existing -> {
            Set<Role> roles = existing.getRoles() != null ? new HashSet<>(existing.getRoles()) : new HashSet<>();
            if (!roles.contains(Role.ADMIN)) {
                roles.add(Role.ADMIN);
                existing.setRoles(roles);
                userRepository.save(existing);
            }
        }, () -> {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin"))
                    .roles(Set.of(Role.ADMIN))
                    .enabled(true)
                    .fullName("Administrator")
                    .build();
            userRepository.save(admin);
        });
    }
}


