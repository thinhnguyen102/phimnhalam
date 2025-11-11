package com.aimovie.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationDTO {
    private String type; // SIMILAR, POPULAR, RECENT, TRENDING, PERSONALIZED
    private String title;
    private String description;
    private List<MovieSearchDTO> movies;
    private long totalMovies;
    private String algorithm; // COLLABORATIVE, CONTENT_BASED, HYBRID
}
