package com.aimovie.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class RatingDTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RatingCreateDTO {
        @NotNull
        private Long movieId;

        @NotNull
        @Min(1)
        @Max(5)
        private Integer stars;

        @Size(max = 1000)
        private String comment;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RatingUpdateDTO {
        private Integer stars;
        private String comment;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RatingResponseDTO {
        private Long id;
        private Long userId;
        private Long movieId;
        private Integer stars;
        private String comment;
        private LocalDateTime createdAt;
    }
}



