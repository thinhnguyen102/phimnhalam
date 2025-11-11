package com.aimovie.repository;

import com.aimovie.entity.Movie;
import com.aimovie.entity.User;
import com.aimovie.entity.WatchHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Long> {

    Page<WatchHistory> findByUserOrderByLastWatchedAtDesc(User user, Pageable pageable);
    Page<WatchHistory> findByUser(User user, Pageable pageable);

    Page<WatchHistory> findByMovieOrderByLastWatchedAtDesc(Movie movie, Pageable pageable);

    Optional<WatchHistory> findByUserAndMovie(User user, Movie movie);

    List<WatchHistory> findByUserAndIsCompletedFalseOrderByLastWatchedAtDesc(User user);

    Page<WatchHistory> findByUserAndIsCompletedTrueOrderByLastWatchedAtDesc(User user, Pageable pageable);


    long countByMovie(Movie movie);

    long countByUserAndIsCompletedTrue(User user);

    @Query("SELECT wh.movie, COUNT(wh) as viewCount FROM WatchHistory wh GROUP BY wh.movie ORDER BY viewCount DESC")
    List<Object[]> findMostWatchedMovies(Pageable pageable);

    @Query("SELECT DATE(wh.lastWatchedAt) as watchDate, COUNT(wh) as viewCount FROM WatchHistory wh WHERE wh.user = :user AND wh.lastWatchedAt >= :startDate GROUP BY DATE(wh.lastWatchedAt) ORDER BY watchDate DESC")
    List<Object[]> findWatchStatsByDate(@Param("user") User user, @Param("startDate") LocalDateTime startDate);

    // Genre query removed
    // Method removed due to genres removal

    void deleteByLastWatchedAtBefore(LocalDateTime cutoffDate);
    void deleteByUser(User user);
    
    void deleteByMovie(Movie movie);

    @Query("SELECT wh.movie, COUNT(wh) as viewCount FROM WatchHistory wh WHERE wh.lastWatchedAt >= :startDate AND wh.lastWatchedAt <= :endDate GROUP BY wh.movie ORDER BY viewCount DESC")
    List<Object[]> findMostWatchedMoviesInPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);
}
