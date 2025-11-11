package com.aimovie.repository;

import com.aimovie.entity.Actor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface ActorRepository extends JpaRepository<Actor, Long> {
    Page<Actor> findByNameContainingIgnoreCase(String q, Pageable pageable);
    Optional<Actor> findByName(String name);
    boolean existsByName(String name);

    @Query("SELECT LOWER(a.name) FROM Actor a WHERE LOWER(a.name) IN :namesLower")
    List<String> findExistingLowerNames(@Param("namesLower") List<String> namesLower);
}


