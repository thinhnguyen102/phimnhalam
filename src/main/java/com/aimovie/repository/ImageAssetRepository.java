package com.aimovie.repository;

import com.aimovie.entity.ImageAsset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageAssetRepository extends JpaRepository<ImageAsset, Long> {
    Page<ImageAsset> findByOwnerId(Long ownerId, Pageable pageable);
}



