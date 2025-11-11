package com.aimovie.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminMovieDTO {
    private Long id;
    private String title;
    private String synopsis;
    private Integer year;
    private List<String> categories; 
    private DirectorDTO director;
    private List<String> actors;
    private String posterUrl;
    private String thumbnailUrl;
    private String videoUrl;
    private String videoQuality;
    private Integer videoDuration;
    private Long fileSizeBytes;
    private String language;
    private String ageRating;
    private Double imdbRating;
    private Boolean isFeatured;
    private Boolean isTrending;
    private java.time.LocalDate releaseDate;
    private String trailerUrl;
    private List<String> availableQualities;
    private String countryName;
    private Boolean isAvailable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long viewCount;
    private long commentCount;
    private double averageRating;
    private String uploaderUsername;
}
