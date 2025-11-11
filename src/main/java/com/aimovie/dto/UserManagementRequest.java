package com.aimovie.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserManagementRequest {
    private Long userId;
    private boolean enabled;
    private List<String> roles;
    private String reason;
}
