package com.aimovie.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialLoginRequest {
    private String provider; // GOOGLE, FACEBOOK, APPLE
    private String providerId;
    private String email;
    private String fullName;
    private String name;
    private String avatarUrl;
    private String accessToken;
}
