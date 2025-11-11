package com.aimovie.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchHistoryDTO {
    private Long id;
    private Long movieId;
    private String movieTitle;
    private String moviePosterUrl;
    private Integer watchDurationSeconds; // in seconds
    private Integer watchedDuration; // in seconds
    private Integer totalDuration; // in seconds
    private Integer totalDurationSeconds; // in seconds
    private Double progressPercentage;
    private Double watchPercentage;
    private String deviceType;
    private String quality;
    private String subtitleLanguage;
    private Boolean isCompleted;
    private LocalDateTime lastWatchedAt;
    private LocalDateTime createdAt;
}
