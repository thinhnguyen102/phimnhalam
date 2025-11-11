package com.aimovie.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminStatsDTO {
    private long totalUsers;
    private long totalMovies;
    private long totalComments;
    private long totalReports;
    private long pendingReports;
    private long pendingComments;
    private long activeUsers;
    private long disabledUsers;
    private double averageRating;
    private long totalViews;
    private MonthlyStatsDTO monthlyStats;
}
