package com.aimovie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class CharacterDTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CharacterCreateDTO {
        @NotBlank
        @Size(max = 255)
        private String name;

        private List<String> aliases = new ArrayList<>();

        @Size(max = 500)
        private String portraitUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CharacterUpdateDTO {
        private String name;
        private List<String> aliases;
        private String portraitUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CharacterResponseDTO {
        private Long id;
        private String name;
        private List<String> aliases;
        private String portraitUrl;
    }
}



