package com.aimovie.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;
    private List<String> roles;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private long totalComments;
    private long totalReports;
    private long totalMovies;
}
