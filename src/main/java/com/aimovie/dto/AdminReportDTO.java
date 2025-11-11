package com.aimovie.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminReportDTO {
    private Long id;
    private String reason;
    private String description;
    private String reporterUsername;
    private String reportedUserUsername;
    private String reportedCommentContent;
    private String reportedMovieTitle;
    private String reportType;
    private String status;
    private String resolvedByUsername;
    private String resolutionNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
