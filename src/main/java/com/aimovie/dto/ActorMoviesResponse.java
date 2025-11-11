package com.aimovie.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActorMoviesResponse {
    private String actorName;
    private List<MovieSearchDTO> movies;
    private Long totalMovies;
    private Double averageRating;
    private String mostPopularGenre;
    private List<String> allGenres;
    private Integer totalViewCount;
}
