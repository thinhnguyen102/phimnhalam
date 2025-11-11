package com.aimovie.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileUpdateRequest {
    private String fullName;
    private String phoneNumber;
    private LocalDate birthday;
    private String preferredLanguage;
    private String preferredQuality;
    private Boolean autoPlay;
    private Boolean subtitleEnabled;
}
