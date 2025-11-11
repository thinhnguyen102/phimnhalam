package com.aimovie.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiServiceConfig {
    private String baseUrl;
    private String endpoint;
    private Integer timeoutSeconds;
    private String apiKey;
    private String modelVersion;
    private Double defaultConfidenceThreshold;
}
