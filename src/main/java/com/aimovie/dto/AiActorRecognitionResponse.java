package com.aimovie.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiActorRecognitionResponse {
    private boolean success;
    private String message;
    private List<ActorRecognition> actors;
    private Double processingTime;
    private String modelVersion;
}
