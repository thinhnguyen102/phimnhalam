package com.aimovie.repository;

import com.aimovie.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDate;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    @EntityGraph(attributePaths = {"director", "country", "categories"})
    Page<Movie> findByTitleContainingIgnoreCase(String q, Pageable pageable);
    
    @EntityGraph(attributePaths = {"director", "country", "categories"})
    @Query("SELECT m FROM Movie m WHERE m.title ILIKE %:q% AND (m.releaseDate IS NULL OR m.releaseDate <= :today)")
    Page<Movie> findByTitleContainingIgnoreCaseAndReleased(@Param("q") String q, @Param("today") LocalDate today, Pageable pageable);
    
    @EntityGraph(attributePaths = {"director", "country", "categories"})
    @Query("SELECT m FROM Movie m WHERE m.year = :year")
    Page<Movie> findByYear(@Param("year") Integer year, Pageable pageable);
    
    @EntityGraph(attributePaths = {"director", "country", "categories"})
    @Query("SELECT m FROM Movie m WHERE m.year = :year AND (m.releaseDate IS NULL OR m.releaseDate <= :today)")
    Page<Movie> findByYearAndReleased(@Param("year") Integer year, @Param("today") LocalDate today, Pageable pageable);
    
    @EntityGraph(attributePaths = {"director", "country", "categories"})
    @Query("SELECT m FROM Movie m WHERE m.year BETWEEN :startYear AND :endYear")
    Page<Movie> findByYearBetween(@Param("startYear") Integer startYear, @Param("endYear") Integer endYear, Pageable pageable);
    
    @EntityGraph(attributePaths = {"director", "country", "categories"})
    @Query("SELECT m FROM Movie m WHERE m.year BETWEEN :startYear AND :endYear AND (m.releaseDate IS NULL OR m.releaseDate <= :today)")
    Page<Movie> findByYearBetweenAndReleased(@Param("startYear") Integer startYear, @Param("endYear") Integer endYear, @Param("today") LocalDate today, Pageable pageable);
    
    @EntityGraph(attributePaths = {"director", "country", "categories"})
    Page<Movie> findByIsAvailableTrue(Pageable pageable);
    
    @EntityGraph(attributePaths = {"director", "country", "categories"})
    @Query("SELECT m FROM Movie m WHERE m.isAvailable = true AND (m.releaseDate IS NULL OR m.releaseDate <= :today)")
    Page<Movie> findByIsAvailableTrueAndReleased(@Param("today") LocalDate today, Pageable pageable);
    
    @EntityGraph(attributePaths = {"director", "country", "categories"})
    Page<Movie> findByIsAvailableFalse(Pageable pageable);
    
    @EntityGraph(attributePaths = {"director", "country", "categories"})
    @Query("SELECT m FROM Movie m WHERE m.title ILIKE %:title% OR m.synopsis ILIKE %:synopsis%")
    Page<Movie> findByTitleOrSynopsisContainingIgnoreCase(@Param("title") String title, @Param("synopsis") String synopsis, Pageable pageable);
    
    @EntityGraph(attributePaths = {"director", "country", "categories"})
    @Query("SELECT m FROM Movie m WHERE (m.title ILIKE %:title% OR m.synopsis ILIKE %:synopsis%) AND (m.releaseDate IS NULL OR m.releaseDate <= :today)")
    Page<Movie> findByTitleOrSynopsisContainingIgnoreCaseAndReleased(@Param("title") String title, @Param("synopsis") String synopsis, @Param("today") LocalDate today, Pageable pageable);
    
    @Query("SELECT DISTINCT m.year FROM Movie m WHERE m.year IS NOT NULL ORDER BY m.year DESC")
    List<Integer> findAllDistinctYears();
    
    // Actor-related queries
    @EntityGraph(attributePaths = {"director", "country", "categories"})
    @Query("SELECT m FROM Movie m WHERE :actor MEMBER OF m.actors")
    Page<Movie> findByActorsContaining(@Param("actor") String actor, Pageable pageable);
    
    @EntityGraph(attributePaths = {"director", "country", "categories"})
    @Query("SELECT m FROM Movie m WHERE LOWER(:actor) IN (SELECT LOWER(a) FROM m.actors a)")
    Page<Movie> findByActorsContainingIgnoreCase(@Param("actor") String actor, Pageable pageable);
    
    @EntityGraph(attributePaths = {"director", "country", "categories"})
    @Query("SELECT m FROM Movie m WHERE EXISTS (SELECT 1 FROM m.actors a WHERE LOWER(a) LIKE LOWER(CONCAT('%', :actor, '%')))")
    Page<Movie> findByActorsLikeIgnoreCase(@Param("actor") String actor, Pageable pageable);
    
    @EntityGraph(attributePaths = {"director", "country", "categories"})
    @Query("SELECT m FROM Movie m WHERE EXISTS (SELECT 1 FROM m.actors a WHERE LOWER(a) LIKE LOWER(CONCAT('%', :actor, '%'))) AND (m.releaseDate IS NULL OR m.releaseDate <= :today)")
    Page<Movie> findByActorsLikeIgnoreCaseAndReleased(@Param("actor") String actor, @Param("today") LocalDate today, Pageable pageable);
    
    @Query("SELECT DISTINCT actor FROM Movie m JOIN m.actors actor ORDER BY actor")
    List<String> findAllDistinctActors();
    
    @EntityGraph(attributePaths = {"director", "country", "categories"})
    @Query("SELECT m FROM Movie m WHERE LOWER(m.director.name) LIKE LOWER(CONCAT('%', :director, '%'))")
    Page<Movie> findByDirectorNameContainingIgnoreCase(@Param("director") String director, Pageable pageable);
    
    @EntityGraph(attributePaths = {"director", "country", "categories"})
    @Query("SELECT m FROM Movie m WHERE LOWER(m.director.name) LIKE LOWER(CONCAT('%', :director, '%')) AND (m.releaseDate IS NULL OR m.releaseDate <= :today)")
    Page<Movie> findByDirectorNameContainingIgnoreCaseAndReleased(@Param("director") String director, @Param("today") LocalDate today, Pageable pageable);
    
    @Query("SELECT DISTINCT m.director.name FROM Movie m WHERE m.director IS NOT NULL ORDER BY m.director.name")
    List<String> findAllDistinctDirectors();
    
    // Popular actors query
    @Query("SELECT actor, COUNT(m) as movieCount FROM Movie m JOIN m.actors actor GROUP BY actor ORDER BY movieCount DESC")
    List<Object[]> findPopularActors(Pageable pageable);
    
    // Movies by actor and genre - removed due to genres removal
    
    // Movies by multiple actors
    @EntityGraph(attributePaths = {"director", "country", "categories"})
    @Query("SELECT m FROM Movie m WHERE EXISTS (SELECT 1 FROM m.actors a WHERE a IN :actors)")
    Page<Movie> findByActorsIn(@Param("actors") List<String> actors, Pageable pageable);
    
    // Featured and trending movies
    @EntityGraph(attributePaths = {"director", "country", "categories"})
    Page<Movie> findByIsFeaturedTrueAndIsAvailableTrue(Pageable pageable);
    
    @EntityGraph(attributePaths = {"director", "country", "categories"})
    @Query("SELECT m FROM Movie m WHERE m.isFeatured = true AND m.isAvailable = true AND (m.releaseDate IS NULL OR m.releaseDate <= :today)")
    Page<Movie> findByIsFeaturedTrueAndIsAvailableTrueAndReleased(@Param("today") LocalDate today, Pageable pageable);
    
    @EntityGraph(attributePaths = {"director", "country", "categories"})
    Page<Movie> findByIsTrendingTrueAndIsAvailableTrue(Pageable pageable);
    
    // Country-related queries
    @EntityGraph(attributePaths = {"director", "country", "categories"})
    List<Movie> findByCountry(com.aimovie.entity.Country country);
    Long countByCountry(com.aimovie.entity.Country country);
    
    // Override findAll to include director, country, and categories
    @EntityGraph(attributePaths = {"director", "country", "categories"})
    @Override
    List<Movie> findAll();
    
    Page<Movie> findByStatus(String status, Pageable pageable);
    
    @EntityGraph(attributePaths = {"director", "country", "categories"})
    @Query("SELECT m FROM Movie m WHERE m.averageRating BETWEEN :minRating AND :maxRating")
    Page<Movie> findByAverageRatingBetween(@Param("minRating") double minRating, @Param("maxRating") double maxRating, Pageable pageable);
    
    @EntityGraph(attributePaths = {"director", "country", "categories"})
    @Query("SELECT m FROM Movie m WHERE m.averageRating BETWEEN :minRating AND :maxRating AND (m.releaseDate IS NULL OR m.releaseDate <= :today)")
    Page<Movie> findByAverageRatingBetweenAndReleased(@Param("minRating") double minRating, @Param("maxRating") double maxRating, @Param("today") LocalDate today, Pageable pageable);

    @EntityGraph(attributePaths = {"director", "country", "categories"})
    @Query("SELECT m FROM Movie m WHERE m.releaseDate > :today ORDER BY m.releaseDate ASC")
    Page<Movie> findUpcomingByReleaseDate(@Param("today") LocalDate today, Pageable pageable);

    @EntityGraph(attributePaths = {"director", "country", "categories"})
    @Query("SELECT m FROM Movie m WHERE m.releaseDate <= :today ORDER BY m.releaseDate DESC")
    Page<Movie> findNowShowingByReleaseDate(@Param("today") LocalDate today, Pageable pageable);

    @EntityGraph(attributePaths = {"director", "country", "categories"})
    @Query("SELECT m FROM Movie m WHERE m.isTrending = true AND m.isAvailable = true AND (m.releaseDate IS NULL OR m.releaseDate <= :today)")
    Page<Movie> findTrendingNowShowing(@Param("today") LocalDate today, Pageable pageable);
}



