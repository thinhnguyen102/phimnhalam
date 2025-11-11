package com.aimovie.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicRatingDTO {
    private Long id;
    private Integer stars;
    private String comment;
    private String username;
    private String userAvatarUrl;
    private String movieTitle;
    private Long movieId;
    private String moviePosterUrl;
    private LocalDateTime createdAt;
}

