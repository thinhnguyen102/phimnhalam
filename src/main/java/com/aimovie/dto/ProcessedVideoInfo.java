package com.aimovie.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedVideoInfo {
    private String quality;
    private String outputPath;
    private long fileSizeBytes;
    private int width;
    private int height;
    private int bitrate;
    private String format;
    private boolean success;
    private String errorMessage;
}


