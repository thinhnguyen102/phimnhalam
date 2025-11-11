package com.aimovie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class CountryDTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CountryCreateRequest {
        @NotBlank(message = "Country name is required")
        @Size(max = 100, message = "Country name must not exceed 100 characters")
        private String name;

        @Size(max = 500, message = "Flag URL must not exceed 500 characters")
        private String flagUrl;

        @Builder.Default
        private Boolean isActive = true;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CountryUpdateRequest {
        @Size(max = 100, message = "Country name must not exceed 100 characters")
        private String name;

        @Size(max = 500, message = "Flag URL must not exceed 500 characters")
        private String flagUrl;

        private Boolean isActive;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CountryResponse {
        private Long id;
        private String name;
        private String flagUrl;
        private Integer movieCount;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CountrySummaryResponse {
        private Long id;
        private String name;
        private String flagUrl;
        private Integer movieCount;
        private Boolean isActive;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CountryWithMoviesResponse {
        private Long id;
        private String name;
        private String flagUrl;
        private Integer movieCount;
        private Boolean isActive;
        private List<MovieSummaryDTO> movies;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MovieSummaryDTO {
        private Long id;
        private String title;
        private String posterUrl;
        private Integer year;
        private Double averageRating;
        private Long viewCount;
    }

    // Form-based DTOs for create/update operations
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CountryFormCreateRequest {
        @NotBlank(message = "Country name is required")
        @Size(max = 100, message = "Country name must not exceed 100 characters")
        private String name;

        @Size(max = 500, message = "Flag URL must not exceed 500 characters")
        private String flagUrl;

        @Builder.Default
        private Boolean isActive = true;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CountryFormUpdateRequest {
        @Size(max = 100, message = "Country name must not exceed 100 characters")
        private String name;

        @Size(max = 500, message = "Flag URL must not exceed 500 characters")
        private String flagUrl;

        private Boolean isActive;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CountryFormResponse {
        private Long id;
        private String name;
        private String flagUrl;
        private Integer movieCount;
        private Boolean isActive;
        private String message;
        private Boolean success;
    }

    // Bulk operations DTOs
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CountryBulkCreateRequest {
        private List<CountryCreateRequest> countries;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CountryBulkUpdateRequest {
        private List<CountryUpdateRequest> countries;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CountryBulkResponse {
        private Integer totalProcessed;
        private Integer successCount;
        private Integer failureCount;
        private List<String> errors;
        private List<CountryResponse> createdCountries;
    }
}
