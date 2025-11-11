package com.aimovie.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActorRecognition {
    private String actorName;
    private Double confidence;
    private String faceBoundingBox;
    private String gender;
    private Integer age;
    private String ethnicity;
}
