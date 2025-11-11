package com.aimovie.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiServiceHealth {
    private boolean isHealthy;
    private String status;
    private String version;
    private Long responseTime;
    private String lastChecked;
    private String errorMessage;
}
