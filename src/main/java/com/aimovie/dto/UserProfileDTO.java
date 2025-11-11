package com.aimovie.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private LocalDate birthday;
    private String avatarUrl;
    private String preferredLanguage;
    private String preferredQuality;
    private Boolean autoPlay;
    private Boolean subtitleEnabled;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
