package com.aimovie.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MovieDTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MovieCreateDTO {
        @NotBlank
        @Size(max = 255)
        private String title;

        @Size(max = 2000)
        private String synopsis;

        @Min(1888)
        @Max(2100)
        private Integer year;

        // Genres removed in favor of categories
        
        // Category names (ManyToMany by name)
        private List<String> categories = new ArrayList<>();

        @Size(max = 500)
        private String posterUrl;
        @Size(max = 500)
        private String thumbnailUrl;

        // Video content fields
        @Size(max = 1000)
        private String videoUrl;

        @Size(max = 100)
        private String videoFormat;

        private Integer videoDuration;

        @Size(max = 100)
        private String videoQuality;

        private Long fileSizeBytes;

        @Size(max = 1000)
        private String streamingUrl;

        private Boolean isAvailable;

        // Additional movie details
        private List<String> actors = new ArrayList<>();
        
        @NotBlank
        private String directorName;
        
        @Size(max = 100)
        private String country;
        
        @Size(max = 100)
        private String language;
        
        @Size(max = 10)
        private String ageRating;
        
        @Min(0)
        @Max(10)
        private Double imdbRating;
        
        private Long viewCount = 0L;
        private Long likeCount = 0L;
        private Long dislikeCount = 0L;
        private Double averageRating = 0.0;
        private Long totalRatings = 0L;
        private Long commentCount = 0L;
        private Boolean isFeatured = false;
        private Boolean isTrending = false;
        private java.time.LocalDate releaseDate;
        
        @Size(max = 1000)
        private String trailerUrl;
        
        private Boolean downloadEnabled = false;
        
        @Size(max = 20)
        private String maxDownloadQuality;
        
        private List<String> availableQualities = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MovieUpdateDTO {
        private String title;
        private String synopsis;
        private Integer year;
        // Genres removed in favor of categories
        private List<String> categories;
        private String posterUrl;
        private String thumbnailUrl;
        
        // Video content fields
        private String videoUrl;
        private String videoFormat;
        private Integer videoDuration;
        private String videoQuality;
        private Long fileSizeBytes;
        private String streamingUrl;
        private Boolean isAvailable;
        
        // Additional movie details
        private List<String> actors;
        private String directorName;
        private String country;
        private String language;
        private String ageRating;
        private Double imdbRating;
        private Long viewCount;
        private Long likeCount;
        private Long dislikeCount;
        private Double averageRating;
        private Long totalRatings;
        private Long commentCount;
        private Boolean isFeatured;
        private Boolean isTrending;
        private java.time.LocalDate releaseDate;
        private String trailerUrl;
        private Boolean downloadEnabled;
        private String maxDownloadQuality;
        private List<String> availableQualities;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MovieResponseDTO {
        private Long id;
        private String title;
        private String synopsis;
        private Integer year;
        private List<String> categories;
        private String posterUrl;
        private String thumbnailUrl;
        
        // Video content fields
        private String videoUrl;
        private String videoFormat;
        private Integer videoDuration;
        private String videoQuality;
        private Long fileSizeBytes;
        private String streamingUrl;
        private Boolean isAvailable;
        
        private List<String> actors;
        private String directorName;
        private List<ActorCRUD.Response> actorDetails;
        private String country;
        private String language;
        private String ageRating;
        private Double imdbRating;
        private Long viewCount;
        private Long likeCount;
        private Long dislikeCount;
        private Double averageRating;
        private Long totalRatings;
        private Boolean isFeatured;
        private Boolean isTrending;
        private java.time.LocalDate releaseDate;
        private String trailerUrl;
        private Boolean downloadEnabled;
        private String maxDownloadQuality;
        private List<String> availableQualities;
        
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}


