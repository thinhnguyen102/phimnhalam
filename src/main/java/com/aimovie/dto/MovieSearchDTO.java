package com.aimovie.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieSearchDTO {
    private Long id;
    private String title;
    private String synopsis;
    private Integer year;
    private List<String> actors;
    private String directorName;
    private String country;
    private String language;
    private String ageRating;
    private Double imdbRating;
    private Double averageRating;
    private Long viewCount;
    private String posterUrl;
    private String thumbnailUrl;
    private String trailerUrl;
    private Boolean isFeatured;
    private Boolean isTrending;
    private LocalDate releaseDate;
    private Boolean isInWatchlist;
    private Boolean isFavorite;
}
