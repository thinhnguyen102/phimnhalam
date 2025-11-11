package com.aimovie.repository;

import com.aimovie.entity.Appearance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppearanceRepository extends JpaRepository<Appearance, Long> {
    List<Appearance> findByMovieId(Long movieId);
    List<Appearance> findByCharacterId(Long characterId);
    boolean existsByMovieIdAndCharacterId(Long movieId, Long characterId);
    
    void deleteByMovieId(Long movieId);
}



