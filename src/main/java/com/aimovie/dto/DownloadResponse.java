package com.aimovie.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DownloadResponse {
    private Long id;
    private Long movieId;
    private String movieTitle;
    private String fileName;
    private String downloadUrl;
    private Long fileSize;
    private String quality;
    private String format;
    private Long fileSizeBytes;
    private String fileSizeFormatted;
    private Integer duration; 
    private String status; 
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private String errorMessage;
}
