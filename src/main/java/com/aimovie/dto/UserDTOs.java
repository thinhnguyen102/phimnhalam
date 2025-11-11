package com.aimovie.dto;

import com.aimovie.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public class UserDTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserCreateDTO {
        @NotBlank
        @Size(max = 50)
        private String username;

        @NotBlank
        @Email
        @Size(max = 255)
        private String email;

        @NotBlank
        @Size(max = 255)
        private String password;

        @Size(max = 100)
        private String fullName;

        private LocalDate birthday;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserUpdateDTO {
        @Email
        @Size(max = 255)
        private String email;

        @Size(max = 100)
        private String fullName;

        private LocalDate birthday;

        @Size(max = 500)
        private String avatarUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserResponseDTO {
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
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePasswordDTO {
        @NotBlank
        @Size(max = 255)
        private String currentPassword;

        @NotBlank
        @Size(max = 255)
        private String newPassword;

        @NotBlank
        @Size(max = 255)
        private String confirmPassword;
    }
}



