package com.aimovie.repository;

import com.aimovie.entity.Favorite;
import com.aimovie.entity.Movie;
import com.aimovie.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Page<Favorite> findByUserAndIsFavoriteTrueOrderByAddedAtDesc(User user, Pageable pageable);

    Optional<Favorite> findByUserAndMovie(User user, Movie movie);

    boolean existsByUserAndMovieAndIsFavoriteTrue(User user, Movie movie);

    long countByUserAndIsFavoriteTrue(User user);

    @Query("SELECT f.movie, COUNT(f) as favoriteCount FROM Favorite f WHERE f.isFavorite = true GROUP BY f.movie ORDER BY favoriteCount DESC")
    List<Object[]> findMostFavoritedMovies(Pageable pageable);

    // Genre query removed
    // Method removed due to genres removal


    void deleteByUser(User user);

    void deleteByMovie(Movie movie);
}
