package com.aimovie.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteDTO {
    private Long id;
    private Long movieId;
    private String movieTitle;
    private String moviePosterUrl;
    private Integer movieYear;
    private List<String> movieGenres;
    private Double movieRating;
    private String movieSynopsis;
    private LocalDateTime addedAt;
}
