package com.aimovie.repository;

import com.aimovie.entity.Movie;
import com.aimovie.entity.Watchlist;
import com.aimovie.entity.WatchlistCollection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    // New methods for WatchlistCollection
    List<Watchlist> findByWatchlistCollection(WatchlistCollection watchlistCollection);
    
    List<Watchlist> findByWatchlistCollectionOrderByPriorityDescAddedAtDesc(WatchlistCollection watchlistCollection);

    Page<Watchlist> findByWatchlistCollectionOrderByPriorityDescAddedAtDesc(WatchlistCollection watchlistCollection, Pageable pageable);

    Optional<Watchlist> findByWatchlistCollectionAndMovie(WatchlistCollection watchlistCollection, Movie movie);

    boolean existsByWatchlistCollectionAndMovie(WatchlistCollection watchlistCollection, Movie movie);

    long countByWatchlistCollection(WatchlistCollection watchlistCollection);

    // Genre query removed
    // Method removed due to genres removal

    @Query("SELECT w FROM Watchlist w WHERE w.watchlistCollection.id = :collectionId ORDER BY w.priority DESC, w.addedAt DESC")
    List<Watchlist> findByWatchlistCollectionIdOrderByPriorityDescAddedAtDesc(@Param("collectionId") Long collectionId);

    @Query("SELECT w FROM Watchlist w WHERE w.watchlistCollection.id = :collectionId AND w.movie.id = :movieId")
    Optional<Watchlist> findByWatchlistCollectionIdAndMovieId(@Param("collectionId") Long collectionId, @Param("movieId") Long movieId);

    @Query("SELECT COUNT(w) FROM Watchlist w WHERE w.watchlistCollection.id = :collectionId")
    Long countByWatchlistCollectionId(@Param("collectionId") Long collectionId);

    @Query("UPDATE Watchlist w SET w.priority = :priority WHERE w.id = :id")
    void updatePriority(@Param("id") Long id, @Param("priority") Integer priority);

    void deleteByWatchlistCollection(WatchlistCollection watchlistCollection);

    void deleteByMovie(Movie movie);

    // Legacy methods for backward compatibility (deprecated)
    @Deprecated
    @Query("SELECT w FROM Watchlist w WHERE w.watchlistCollection.user.id = :userId AND w.isInWatchlist = true ORDER BY w.priority DESC, w.addedAt DESC")
    Page<Watchlist> findByUserIdAndIsInWatchlistTrueOrderByPriorityDescAddedAtDesc(@Param("userId") Long userId, Pageable pageable);

    @Deprecated
    @Query("SELECT w FROM Watchlist w WHERE w.watchlistCollection.user.id = :userId AND w.movie.id = :movieId")
    Optional<Watchlist> findByUserIdAndMovieId(@Param("userId") Long userId, @Param("movieId") Long movieId);

    @Deprecated
    @Query("SELECT COUNT(w) FROM Watchlist w WHERE w.watchlistCollection.user.id = :userId AND w.isInWatchlist = true")
    long countByUserIdAndIsInWatchlistTrue(@Param("userId") Long userId);

    @Deprecated
    @Query("SELECT w FROM Watchlist w WHERE w.watchlistCollection.user.id = :userId AND w.isInWatchlist = true ORDER BY w.priority DESC")
    List<Watchlist> findByUserIdAndIsInWatchlistTrueOrderByPriorityDesc(@Param("userId") Long userId);

    @Deprecated
    @Query("SELECT w FROM Watchlist w WHERE w.watchlistCollection.user.id = :userId AND w.isInWatchlist = true ORDER BY w.addedAt DESC")
    List<Watchlist> findByUserIdAndIsInWatchlistTrueOrderByAddedAtDesc(@Param("userId") Long userId);

    @Deprecated
    @Query("SELECT COUNT(w) FROM Watchlist w WHERE w.watchlistCollection.user.id = :userId AND w.movie.id = :movieId AND w.isInWatchlist = true")
    boolean existsByUserIdAndMovieIdAndIsInWatchlistTrue(@Param("userId") Long userId, @Param("movieId") Long movieId);

    @Deprecated
    @Query("SELECT w FROM Watchlist w WHERE w.watchlistCollection.user.id = :userId")
    List<Watchlist> findByUserId(@Param("userId") Long userId);

    @Deprecated
    @Modifying
    @Query("DELETE FROM Watchlist w WHERE w.watchlistCollection.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Deprecated
    @Modifying
    @Query("DELETE FROM Watchlist w WHERE w.watchlistCollection.user.id = :userId AND w.movie.id = :movieId")
    void deleteByUserIdAndMovieId(@Param("userId") Long userId, @Param("movieId") Long movieId);
}