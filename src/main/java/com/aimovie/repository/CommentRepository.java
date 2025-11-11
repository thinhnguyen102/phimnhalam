package com.aimovie.repository;

import com.aimovie.entity.Comment;
import com.aimovie.entity.Movie;
import com.aimovie.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByMovieAndIsDeletedFalseOrderByCreatedAtDesc(Movie movie, Pageable pageable);

    Page<Comment> findByIsApprovedFalseAndIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    Page<Comment> findByIsDeletedTrueOrderByCreatedAtDesc(Pageable pageable);

    Page<Comment> findByUserAndIsDeletedFalseOrderByCreatedAtDesc(User user, Pageable pageable);

    List<Comment> findByParentCommentAndIsDeletedFalseOrderByCreatedAtAsc(Comment parentComment);

    long countByMovieAndIsDeletedFalse(Movie movie);

    long countByIsApprovedFalseAndIsDeletedFalse();

    @Query("SELECT c FROM Comment c WHERE c.content LIKE %:keyword% AND c.isDeleted = false ORDER BY c.createdAt DESC")
    Page<Comment> findByContentContainingAndIsDeletedFalse(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Comment c WHERE YEAR(c.createdAt) = :year AND MONTH(c.createdAt) = :month AND c.isDeleted = false")
    long countByYearAndMonth(@Param("year") int year, @Param("month") int month);

    void deleteByUser(User user);
    
    void deleteByMovie(Movie movie);
}
