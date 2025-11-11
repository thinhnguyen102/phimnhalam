package com.aimovie.service;

import com.aimovie.entity.WatchHistory;
import com.aimovie.entity.User;
import com.aimovie.entity.Movie;

import java.util.List;

public interface WatchHistoryService {

    /**
     * Save or update the watch progress for a user and movie.
     *
     * @param user the user watching the movie
     * @param movie the movie being watched
     * @param watchDurationSeconds the duration watched in seconds
     * @param isCompleted whether the movie is completed
     * @return the updated WatchHistory entity
     */
    WatchHistory saveProgress(User user, Movie movie, int watchDurationSeconds, boolean isCompleted);

    /**
     * Fetch all movies that the user has started but not completed.
     *
     * @param user the user
     * @return a list of WatchHistory entries for incomplete movies
     */
    List<WatchHistory> getIncompleteWatchHistory(User user);
}