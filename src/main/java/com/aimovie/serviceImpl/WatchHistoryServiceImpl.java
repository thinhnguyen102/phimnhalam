package com.aimovie.serviceImpl;

import com.aimovie.entity.Movie;
import com.aimovie.entity.User;
import com.aimovie.entity.WatchHistory;
import com.aimovie.repository.WatchHistoryRepository;
import com.aimovie.service.WatchHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WatchHistoryServiceImpl implements WatchHistoryService {

    private final WatchHistoryRepository watchHistoryRepository;

    @Autowired
    public WatchHistoryServiceImpl(WatchHistoryRepository watchHistoryRepository) {
        this.watchHistoryRepository = watchHistoryRepository;
    }

    @Override
    public WatchHistory saveProgress(User user, Movie movie, int watchDurationSeconds, boolean isCompleted) {
        WatchHistory watchHistory = watchHistoryRepository.findByUserAndMovie(user, movie)
                .orElse(WatchHistory.builder()
                        .user(user)
                        .movie(movie)
                        .build());

        watchHistory.setWatchDurationSeconds(watchDurationSeconds);
        watchHistory.setIsCompleted(isCompleted);
        watchHistory.setLastWatchedAt(LocalDateTime.now());
        watchHistory.setWatchPercentage((double) watchDurationSeconds / watchHistory.getTotalDurationSeconds() * 100);

        return watchHistoryRepository.save(watchHistory);
    }

    @Override
    public List<WatchHistory> getIncompleteWatchHistory(User user) {
        return watchHistoryRepository.findByUserAndIsCompletedFalseOrderByLastWatchedAtDesc(user);
    }
}