package com.aimovie.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchHistoryUpdateRequest {
    private Long movieId;
    private Integer watchDurationSeconds; // in seconds
    private Integer totalDurationSeconds; // in seconds
    private String deviceType;
    private String quality;
    private String subtitleLanguage;
    private Integer volumeLevel;
    private Double playbackSpeed;
    private Boolean isCompleted;
}
