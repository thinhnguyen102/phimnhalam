package com.aimovie.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StreamingResponse {
    private Long movieId;
    private String title;
    private String streamingUrl;
    private String streamUrl;
    private String subtitleUrl;
    private String quality;
    private List<String> availableQualities;
    private List<SubtitleDTO> subtitles;
    private List<SubtitleDTO> availableSubtitles;
    private String currentSubtitleLanguage;
    private String subtitleLanguage;
    private Boolean subtitleEnabled;
    private Integer totalDuration; 
    private Integer currentPosition; 
    private Boolean isCompleted;
    private String thumbnailUrl;
    private String posterUrl;
    private String trailerUrl;
    private String downloadUrl;
    private String maxDownloadQuality;
    private Boolean canDownload;
}
