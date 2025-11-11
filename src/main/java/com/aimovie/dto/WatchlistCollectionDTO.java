package com.aimovie.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class WatchlistCollectionDTO {
    private Long id;
    private String name;
    private String description;
    private Long userId;
    private String username;
    private Integer movieCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<WatchlistDTO> movies;
}
