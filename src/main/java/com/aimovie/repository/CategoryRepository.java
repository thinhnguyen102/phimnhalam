package com.aimovie.repository;

import com.aimovie.entity.Category;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    boolean existsByName(String name);
    Page<Category> findByNameContainingIgnoreCase(String q, Pageable pageable);
    List<Category> findByNameIn(Collection<String> names);

    @Query("SELECT LOWER(c.name) FROM Category c WHERE LOWER(c.name) IN :namesLower")
    List<String> findExistingLowerNames(@Param("namesLower") List<String> namesLower);

    @Query("SELECT c FROM Category c WHERE LOWER(c.name) IN :namesLower")
    List<Category> findByNameLowerIn(@Param("namesLower") List<String> namesLower);
}


