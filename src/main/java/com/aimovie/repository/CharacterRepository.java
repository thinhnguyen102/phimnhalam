package com.aimovie.repository;

import com.aimovie.entity.Character;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CharacterRepository extends JpaRepository<Character, Long> {
    boolean existsByNameIgnoreCase(String name);
    List<Character> findByNameContainingIgnoreCase(String name);
}



