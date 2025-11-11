package com.aimovie.repository;

import com.aimovie.entity.Rating;
import com.aimovie.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Page<Rating> findByMovieId(Long movieId, Pageable pageable);
    List<Rating> findByMovieId(Long movieId);
    Page<Rating> findByUserId(Long userId, Pageable pageable);
    Optional<Rating> findByUserIdAndMovieId(Long userId, Long movieId);
    boolean existsByUserIdAndMovieId(Long userId, Long movieId);
    void deleteByUser(User user);
    
    void deleteByMovieId(Long movieId);
    
    long countByCommentIsNotNull();
    
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.comment IS NOT NULL AND YEAR(r.createdAt) = :year AND MONTH(r.createdAt) = :month")
    long countByCommentIsNotNullAndYearAndMonth(@Param("year") int year, @Param("month") int month);
    
    long countByUserIdAndCommentIsNotNull(Long userId);
    
    List<Rating> findTop5ByCommentIsNotNullOrderByCreatedAtDesc();
}



