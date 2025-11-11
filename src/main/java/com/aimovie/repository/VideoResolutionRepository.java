package com.aimovie.repository;

import com.aimovie.entity.Movie;
import com.aimovie.entity.VideoResolution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoResolutionRepository extends JpaRepository<VideoResolution, Long> {

    List<VideoResolution> findByMovieAndIsAvailableTrue(Movie movie);

    List<VideoResolution> findByMovie(Movie movie);

    Optional<VideoResolution> findByMovieAndQualityAndIsAvailableTrue(Movie movie, String quality);

    Optional<VideoResolution> findByMovieAndQuality(Movie movie, String quality);

    List<VideoResolution> findByMovieIdAndIsAvailableTrue(Long movieId);

    List<VideoResolution> findByMovieId(Long movieId);

    @Query("SELECT vr FROM VideoResolution vr WHERE vr.movie.id = :movieId AND vr.quality = :quality AND vr.isAvailable = true")
    Optional<VideoResolution> findByMovieIdAndQualityAndIsAvailableTrue(@Param("movieId") Long movieId, @Param("quality") String quality);

    @Query("SELECT vr FROM VideoResolution vr WHERE vr.movie.id = :movieId AND vr.quality = :quality")
    Optional<VideoResolution> findByMovieIdAndQuality(@Param("movieId") Long movieId, @Param("quality") String quality);

    @Query("SELECT vr FROM VideoResolution vr WHERE vr.movie.id = :movieId AND vr.isAvailable = true ORDER BY vr.height DESC")
    List<VideoResolution> findAvailableResolutionsByMovieIdOrderByHeightDesc(@Param("movieId") Long movieId);

    @Query("SELECT vr FROM VideoResolution vr WHERE vr.movie.id = :movieId AND vr.isAvailable = true ORDER BY vr.height ASC")
    List<VideoResolution> findAvailableResolutionsByMovieIdOrderByHeightAsc(@Param("movieId") Long movieId);

    @Query("SELECT vr.quality FROM VideoResolution vr WHERE vr.movie.id = :movieId AND vr.isAvailable = true ORDER BY vr.height DESC")
    List<String> findAvailableQualitiesByMovieId(@Param("movieId") Long movieId);

    @Query("SELECT vr FROM VideoResolution vr WHERE vr.encodingStatus = :status")
    List<VideoResolution> findByEncodingStatus(@Param("status") String status);

    @Query("SELECT vr FROM VideoResolution vr WHERE vr.movie.id = :movieId AND vr.encodingStatus = :status")
    List<VideoResolution> findByMovieIdAndEncodingStatus(@Param("movieId") Long movieId, @Param("status") String status);

    boolean existsByMovieAndQuality(Movie movie, String quality);

    boolean existsByMovieIdAndQuality(Long movieId, String quality);

    boolean existsByMovie(Movie movie);

    @Query("SELECT COUNT(vr) FROM VideoResolution vr WHERE vr.movie.id = :movieId AND vr.isAvailable = true")
    Long countAvailableResolutionsByMovieId(@Param("movieId") Long movieId);

    @Query("SELECT vr FROM VideoResolution vr WHERE vr.movie.id = :movieId AND vr.isAvailable = true AND vr.height >= :minHeight ORDER BY vr.height ASC")
    List<VideoResolution> findAvailableResolutionsByMovieIdAndMinHeight(@Param("movieId") Long movieId, @Param("minHeight") Integer minHeight);
    
    void deleteByMovie(Movie movie);
}
