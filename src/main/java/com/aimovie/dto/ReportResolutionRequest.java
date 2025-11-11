package com.aimovie.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResolutionRequest {
    private Long reportId;
    private String status;
    private String resolutionNote;
}
