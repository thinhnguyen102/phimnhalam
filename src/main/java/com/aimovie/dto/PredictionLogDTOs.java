package com.aimovie.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class PredictionLogDTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PredictionLogResponseDTO {
        private Long id;
        private Long requesterId;
        private Long imageAssetId;
        private String topCharacterName;
        private String topMovieTitle;
        private Double confidence;
        private LocalDateTime createdAt;
    }
}


