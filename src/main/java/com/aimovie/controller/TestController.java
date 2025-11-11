package com.aimovie.controller;

import com.aimovie.entity.User;
import com.aimovie.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final UserRepository userRepository;

    @GetMapping("/auth")
    public ResponseEntity<Map<String, Object>> testAuth() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null) {
                response.put("authenticated", false);
                response.put("message", "No authentication found");
                return ResponseEntity.ok(response);
            }
            
            String username = authentication.getName();
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            
            response.put("authenticated", true);
            response.put("username", username);
            response.put("authorities", authorities);
            
            // Get user from database
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                response.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "enabled", user.isEnabled(),
                    "roles", user.getRoles()
                ));
            }
            
            log.info("Test auth for user: {} with authorities: {}", username, authorities);
            
        } catch (Exception e) {
            log.error("Error in test auth", e);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin")
    public ResponseEntity<Map<String, Object>> testAdmin() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null) {
                response.put("success", false);
                response.put("message", "No authentication found");
                return ResponseEntity.ok(response);
            }
            
            String username = authentication.getName();
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            
            boolean hasAdminRole = authorities.stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            
            response.put("success", true);
            response.put("username", username);
            response.put("hasAdminRole", hasAdminRole);
            response.put("authorities", authorities);
            
            log.info("Test admin for user: {} - hasAdminRole: {}", username, hasAdminRole);
            
        } catch (Exception e) {
            log.error("Error in test admin", e);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> testUsers() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get all users (for testing)
            long totalUsers = userRepository.count();
            long enabledUsers = userRepository.countByEnabledTrue();
            long disabledUsers = userRepository.countByEnabledFalse();
            
            response.put("success", true);
            response.put("totalUsers", totalUsers);
            response.put("enabledUsers", enabledUsers);
            response.put("disabledUsers", disabledUsers);
            
            // Get admin user specifically
            Optional<User> adminUser = userRepository.findByUsername("admin");
            if (adminUser.isPresent()) {
                User admin = adminUser.get();
                response.put("adminUser", Map.of(
                    "id", admin.getId(),
                    "username", admin.getUsername(),
                    "email", admin.getEmail(),
                    "enabled", admin.isEnabled(),
                    "roles", admin.getRoles(),
                    "createdAt", admin.getCreatedAt()
                ));
            }
            
            log.info("Test users - total: {}, enabled: {}, disabled: {}", totalUsers, enabledUsers, disabledUsers);
            
        } catch (Exception e) {
            log.error("Error in test users", e);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
