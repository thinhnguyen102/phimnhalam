package com.aimovie.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoProcessingResult {
    private boolean success;
    private String message;
    private String originalFileName;
    private Long movieId;
    private List<ProcessedVideoInfo> processedVideos;
    private String errorMessage;
    private long processingTimeMs;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
}

