package com.aimovie.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchlistUpdateRequest {
    private Long movieId;
    private Integer priority;
    private Boolean isInWatchlist;
    private String notes;
}
