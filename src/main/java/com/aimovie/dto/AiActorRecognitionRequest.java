package com.aimovie.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiActorRecognitionRequest {
    private String imageBase64;
    private String imageUrl;
    private String imageFormat;
    private Double confidenceThreshold;
}
