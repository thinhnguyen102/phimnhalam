package com.aimovie.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardDTO {
    private AdminStatsDTO stats;
    private List<AdminReportDTO> recentReports;
    private List<AdminCommentDTO> pendingComments;
    private List<AdminUserDTO> recentUsers;
    private List<AdminMovieDTO> topMovies;
}
