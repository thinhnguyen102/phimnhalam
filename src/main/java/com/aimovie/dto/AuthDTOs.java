package com.aimovie.dto;

import com.aimovie.entity.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public class AuthDTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequestDTO {
        @NotBlank
        private String username;

        @NotBlank
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthResponseDTO {
        private String token;
        private String type;
        private UserInfoDTO user;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfoDTO {
        private Long id;
        private String username;
        private String email;
        private Set<Role> roles;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CurrentUserDTO {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private LocalDate birthday;
        private String avatarUrl;
        private Set<Role> roles;
        private boolean enabled;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime lastLoginAt;
        private boolean isTokenValid;
    }
}
