package com.aimovie.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieStreamingDTO {
    private Long id;
    private String title;
    private String synopsis;
    private Integer year;
    private List<String> actors;
    private String directorName;
    private String country;
    private String language;
    private String ageRating;
    private Double imdbRating;
    private Double averageRating;
    private Long viewCount;
    private String posterUrl;
    private String trailerUrl;
    private List<String> availableQualities;
    private String currentQuality;
    private List<SubtitleDTO> subtitles;
    private String currentSubtitleLanguage;
    private Boolean subtitleEnabled;
    private Integer videoDuration; 
    private Integer totalDuration; 
    private Integer currentPosition; 
    private Integer resumeTime; 
    private Boolean isCompleted;
    private Boolean downloadEnabled;
    private String maxDownloadQuality;
    private Boolean isInWatchlist;
    private Boolean isFavorite;
}
