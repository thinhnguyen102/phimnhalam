package com.aimovie.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieManagementRequest {
    private Long movieId;
    private String title;
    private String synopsis;
    private Integer year;
    private List<String> actors;
    private List<String> categories;
    private String posterUrl;
    private String thumbnailUrl;
    private String videoUrl;
    private String trailerUrl;
    private String videoQuality;
    private Integer videoDuration;
    private Boolean isAvailable;
    private String directorName;
    private String country;
    private String language;
    private String ageRating;
    private Double imdbRating;
    private Boolean isFeatured;
    private Boolean isTrending;
    private String releaseDate;
    private Boolean downloadEnabled;
    private String maxDownloadQuality;
}
