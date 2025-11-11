package com.aimovie.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class ImageAssetDTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImageAssetResponseDTO {
        private Long id;
        private String url;
        private String mediaType;
        private Long sizeBytes;
        private Long ownerId;
        private LocalDateTime createdAt;
    }
}



