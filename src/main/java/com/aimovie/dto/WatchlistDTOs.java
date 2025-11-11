package com.aimovie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class WatchlistDTOs {

    // WatchlistCollection DTOs
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WatchlistCollectionRequest {
        @NotBlank(message = "Watchlist name is required")
        @Size(max = 100, message = "Watchlist name must not exceed 100 characters")
        private String name;

        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;

        @Builder.Default
        private Boolean isPublic = false;

        @Size(max = 7, message = "Color code must be a valid hex color")
        private String colorCode;

        @Size(max = 50, message = "Icon name must not exceed 50 characters")
        private String icon;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WatchlistCollectionResponse {
        private Long id;
        private String name;
        private String description;
        private Long userId;
        private String userName;
        private Boolean isPublic;
        private Boolean isDefault;
        private Integer movieCount;
        private String colorCode;
        private String icon;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<WatchlistItemResponse> movies;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WatchlistCollectionUpdateRequest {
        @Size(max = 100, message = "Watchlist name must not exceed 100 characters")
        private String name;

        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;

        private Boolean isPublic;

        @Size(max = 7, message = "Color code must be a valid hex color")
        private String colorCode;

        @Size(max = 50, message = "Icon name must not exceed 50 characters")
        private String icon;
    }

    // Watchlist Item DTOs
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WatchlistAddRequest {
        @NotNull(message = "Movie ID is required")
        private Long movieId;

        @NotNull(message = "Watchlist collection ID is required")
        private Long watchlistCollectionId;

        @Size(max = 500, message = "Notes must not exceed 500 characters")
        private String notes;

        @Builder.Default
        private Integer priority = 0;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WatchlistItemResponse {
        private Long id;
        private Long movieId;
        private String movieTitle;
        private String moviePosterUrl;
        private String movieSynopsis;
        private Integer movieYear;
        private Double movieRating;
        private String notes;
        private Integer priority;
        private LocalDateTime addedAt;
        private LocalDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WatchlistItemUpdateRequest {
        @Size(max = 500, message = "Notes must not exceed 500 characters")
        private String notes;

        private Integer priority;
    }

    // Legacy DTOs for backward compatibility
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WatchlistAddDTO {
        @NotNull
        private Long movieId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WatchlistItemResponseDTO {
        private Long id;
        private Long movieId;
        private LocalDateTime createdAt;
    }

    // Summary DTOs
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WatchlistSummaryResponse {
        private Long id;
        private String name;
        private String description;
        private Integer movieCount;
        private String colorCode;
        private String icon;
        private Boolean isDefault;
        private LocalDateTime lastUpdated;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserWatchlistOverviewResponse {
        private Long userId;
        private String userName;
        private List<WatchlistSummaryResponse> watchlists;
        private Integer totalWatchlists;
        private Integer totalMovies;
        private WatchlistSummaryResponse defaultWatchlist;
    }
}



