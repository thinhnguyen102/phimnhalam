package com.aimovie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class ActorCRUD {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank
        @Size(max = 150)
        private String name;

        @Size(max = 500)
        private String imageUrl;

        private LocalDate dob;

        @Size(max = 2000)
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @Size(max = 150)
        private String name;

        @Size(max = 500)
        private String imageUrl;

        private LocalDate dob;

        @Size(max = 2000)
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String name;
        private String imageUrl;
        private LocalDate dob;
        private String description;
        private int movieCount;
    }
}


