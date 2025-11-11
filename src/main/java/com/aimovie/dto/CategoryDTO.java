package com.aimovie.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {
    private String name;
    private String displayName;
    private String description;
    private String icon;
    private List<MovieSearchDTO> movies;
    private long totalMovies;
    private String type; // GENRE, YEAR, RATING, FEATURED, TRENDING
}
