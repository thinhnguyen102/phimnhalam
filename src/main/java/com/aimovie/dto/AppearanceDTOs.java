package com.aimovie.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AppearanceDTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppearanceCreateDTO {
        @NotNull
        private Long movieId;

        @NotNull
        private Long characterId;

        @Size(max = 255)
        private String roleName;

        private Integer billingOrder;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppearanceUpdateDTO {
        private String roleName;
        private Integer billingOrder;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AppearanceResponseDTO {
        private Long id;
        private Long movieId;
        private Long characterId;
        private String roleName;
        private Integer billingOrder;
    }
}


