package com.aimovie.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class DirectorDTO {
    private Long id;
    private String name;
    private String biography;
    private LocalDate birthDate;
    private String nationality;
    private String photoUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long movieCount;
}
