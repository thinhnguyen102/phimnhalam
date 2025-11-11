package com.aimovie.repository;

import com.aimovie.entity.PredictionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PredictionLogRepository extends JpaRepository<PredictionLog, Long> {
    Page<PredictionLog> findByRequesterId(Long requesterId, Pageable pageable);
}



