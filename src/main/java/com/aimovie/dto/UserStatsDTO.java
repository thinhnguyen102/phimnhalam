package com.aimovie.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatsDTO {
    private Long userId;
    private String username;
    private long totalWatchTime; // in seconds
    private long totalMoviesWatched;
    private long totalMoviesCompleted;
    private long totalFavorites;
    private long totalWatchlistItems;
    private long totalComments;
    private long totalRatings;
    private double averageRatingGiven;
    private List<String> favoriteGenres;
    private List<String> favoriteActors;
    private List<String> favoriteDirectors;
    private String mostWatchedGenre;
    private String mostWatchedActor;
    private String mostWatchedDirector;
    private LocalDateTime firstWatchDate;
    private LocalDateTime lastWatchDate;
    private long totalDaysActive;
    private double averageWatchTimePerDay; // in seconds
}
