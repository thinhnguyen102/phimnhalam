package com.aimovie.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchlistDTO {
    private Long id;
    private Long movieId;
    private String movieTitle;
    private String moviePosterUrl;
    private Integer movieYear;
    private List<String> movieGenres;
    private Double movieRating;
    private String movieSynopsis;
    private Integer priority;
    private String notes;
    private LocalDateTime addedAt;
}
