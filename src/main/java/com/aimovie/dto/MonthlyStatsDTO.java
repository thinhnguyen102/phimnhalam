package com.aimovie.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyStatsDTO {
    private int year;
    private int month;
    private long newUsers;
    private long newMovies;
    private long newComments;
    private long newReports;
    private long totalViews;
    private double revenue;
}
