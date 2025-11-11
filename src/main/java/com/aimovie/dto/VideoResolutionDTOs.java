package com.aimovie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

public class VideoResolutionDTOs {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VideoResolutionRequest {
        @NotNull(message = "Movie ID is required")
        private Long movieId;

        @NotBlank(message = "Quality is required")
        @Size(max = 20, message = "Quality must not exceed 20 characters")
        private String quality;

        @NotNull(message = "Width is required")
        private Integer width;

        @NotNull(message = "Height is required")
        private Integer height;

        @Size(max = 1000, message = "Video URL must not exceed 1000 characters")
        private String videoUrl;

        @Size(max = 100, message = "Video format must not exceed 100 characters")
        private String videoFormat;

        private Long fileSizeBytes;

        private Integer bitrate;

        @Builder.Default
        private Boolean isAvailable = true;

        @Builder.Default
        private String encodingStatus = "PENDING";

        @Builder.Default
        private Integer encodingProgress = 0;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VideoResolutionResponse {
        private Long id;
        private Long movieId;
        private String quality;
        private Integer width;
        private Integer height;
        private String videoUrl;
        private String videoFormat;
        private Long fileSizeBytes;
        private Integer bitrate;
        private Boolean isAvailable;
        private String encodingStatus;
        private Integer encodingProgress;
        private String movieTitle;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VideoResolutionUpdateRequest {
        @Size(max = 1000, message = "Video URL must not exceed 1000 characters")
        private String videoUrl;

        @Size(max = 100, message = "Video format must not exceed 100 characters")
        private String videoFormat;

        private Long fileSizeBytes;

        private Integer bitrate;

        private Boolean isAvailable;

        private String encodingStatus;

        private Integer encodingProgress;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AvailableResolutionResponse {
        private String quality;
        private Integer width;
        private Integer height;
        private String videoUrl;
        private String videoFormat;
        private Long fileSizeBytes;
        private Integer bitrate;
        private Boolean isAvailable;
        private String encodingStatus;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResolutionChangeRequest {
        @NotNull(message = "Movie ID is required")
        private Long movieId;

        @NotBlank(message = "Quality is required")
        @Size(max = 20, message = "Quality must not exceed 20 characters")
        private String quality;

        private Integer currentTime;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResolutionChangeResponse {
        private String newStreamingUrl;
        private String quality;
        private Integer width;
        private Integer height;
        private Integer bitrate;
        private Boolean success;
        private String message;
    }
}
