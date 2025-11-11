package com.aimovie.repository;

import com.aimovie.entity.Director;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DirectorRepository extends JpaRepository<Director, Long> {
    
    // Find by name (case insensitive)
    @Query("SELECT d FROM Director d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Director> findByNameContainingIgnoreCase(@Param("name") String name);
    
    // Find by nationality
    List<Director> findByNationalityIgnoreCase(String nationality);
    
    // Find active directors
    List<Director> findByIsActiveTrue();
    
    // Find active directors with pagination
    Page<Director> findByIsActiveTrue(Pageable pageable);
    
    // Find by name exact match (case insensitive)
    Optional<Director> findByNameIgnoreCase(String name);
    
    // Check if director exists by name
    boolean existsByNameIgnoreCase(String name);
    
    // Find directors with movies count
    @Query("SELECT d FROM Director d LEFT JOIN d.movies m WHERE d.isActive = true GROUP BY d.id ORDER BY COUNT(m) DESC")
    List<Director> findActiveDirectorsOrderByMovieCount();
    
    // Search directors by multiple criteria
    @Query("SELECT d FROM Director d WHERE " +
           "(:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:nationality IS NULL OR LOWER(d.nationality) LIKE LOWER(CONCAT('%', :nationality, '%'))) AND " +
           "d.isActive = true")
    Page<Director> searchDirectors(@Param("name") String name, 
                                  @Param("nationality") String nationality, 
                                  Pageable pageable);
    
    // Count movies by director
    @Query("SELECT COUNT(m) FROM Movie m WHERE m.director.id = :directorId")
    Long countMoviesByDirectorId(@Param("directorId") Long directorId);
}
