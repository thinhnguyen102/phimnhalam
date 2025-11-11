package com.aimovie.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActorRecognitionResult {
    private AiActorRecognitionResponse recognitionResponse;
    private ActorMoviesResponse moviesResponse;
    private boolean hasMovies;
    private String errorMessage;
}
