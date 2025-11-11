package com.aimovie.repository;

import com.aimovie.entity.Country;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {

    Optional<Country> findByName(String name);

    Optional<Country> findByNameIgnoreCase(String name);

    List<Country> findByIsActiveTrue();

    List<Country> findByIsActiveTrueOrderByNameAsc();

    Page<Country> findByIsActiveTrue(Pageable pageable);

    @Query("SELECT c FROM Country c WHERE c.isActive = true ORDER BY c.movieCount DESC, c.name ASC")
    List<Country> findActiveCountriesOrderByMovieCountDesc();

    @Query("SELECT c FROM Country c WHERE c.isActive = true ORDER BY c.movieCount DESC, c.name ASC")
    Page<Country> findActiveCountriesOrderByMovieCountDesc(Pageable pageable);

    @Query("SELECT c FROM Country c WHERE c.name LIKE %:name% AND c.isActive = true ORDER BY c.name ASC")
    List<Country> findByNameContainingIgnoreCaseAndIsActiveTrue(@Param("name") String name);

    @Query("SELECT c FROM Country c WHERE c.name LIKE %:name% AND c.isActive = true ORDER BY c.name ASC")
    Page<Country> findByNameContainingIgnoreCaseAndIsActiveTrue(@Param("name") String name, Pageable pageable);

    @Query("SELECT c FROM Country c WHERE c.movieCount > :minMovieCount AND c.isActive = true ORDER BY c.movieCount DESC")
    List<Country> findByMovieCountGreaterThanAndIsActiveTrue(@Param("minMovieCount") Integer minMovieCount);

    @Query("SELECT c FROM Country c WHERE c.movieCount BETWEEN :minCount AND :maxCount AND c.isActive = true ORDER BY c.movieCount DESC")
    List<Country> findByMovieCountBetweenAndIsActiveTrue(@Param("minCount") Integer minCount, @Param("maxCount") Integer maxCount);

    @Query("SELECT COUNT(c) FROM Country c WHERE c.isActive = true")
    Long countActiveCountries();

    @Query("SELECT SUM(c.movieCount) FROM Country c WHERE c.isActive = true")
    Long sumMovieCountForActiveCountries();

    @Query("SELECT c FROM Country c WHERE c.isActive = true AND c.movieCount = 0 ORDER BY c.name ASC")
    List<Country> findActiveCountriesWithNoMovies();

    @Query("SELECT c FROM Country c WHERE c.isActive = true AND c.movieCount > 0 ORDER BY c.movieCount DESC, c.name ASC")
    List<Country> findActiveCountriesWithMovies();

    boolean existsByName(String name);

    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT c FROM Country c WHERE c.id != :id AND c.name = :name")
    Optional<Country> findByNameAndIdNot(@Param("name") String name, @Param("id") Long id);

    @Query("SELECT c FROM Country c WHERE c.id != :id AND LOWER(c.name) = LOWER(:name)")
    Optional<Country> findByNameIgnoreCaseAndIdNot(@Param("name") String name, @Param("id") Long id);

    @Query("SELECT c FROM Country c WHERE c.isActive = true ORDER BY c.updatedAt DESC")
    List<Country> findActiveCountriesOrderByUpdatedAtDesc();

    @Query("SELECT c FROM Country c WHERE c.isActive = true ORDER BY c.createdAt DESC")
    List<Country> findActiveCountriesOrderByCreatedAtDesc();

    // Additional methods needed by CountryServiceImpl
    List<Country> findByNameContainingIgnoreCase(String name);
    
    Page<Country> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    @Query("SELECT c FROM Country c ORDER BY c.movieCount DESC, c.name ASC")
    List<Country> findAllOrderByMovieCount();
    
    @Query("SELECT c FROM Country c ORDER BY c.movieCount DESC, c.name ASC")
    Page<Country> findAllOrderByMovieCount(Pageable pageable);
    
    @Query("SELECT c FROM Country c WHERE c.movieCount BETWEEN :minCount AND :maxCount ORDER BY c.movieCount DESC")
    List<Country> findByMovieCountBetween(@Param("minCount") Integer minCount, @Param("maxCount") Integer maxCount);
    
    @Query("SELECT c FROM Country c WHERE c.movieCount = 0 ORDER BY c.name ASC")
    List<Country> findByMovieCount(Integer movieCount);
    
    @Query("SELECT c FROM Country c WHERE c.movieCount > 0 ORDER BY c.movieCount DESC, c.name ASC")
    List<Country> findByMovieCountGreaterThan(Integer movieCount);
    
    @Query("SELECT COUNT(c) FROM Country c WHERE c.isActive = true")
    Long countByIsActiveTrue();
    
    boolean existsByNameAndIdNot(String name, Long id);
}
