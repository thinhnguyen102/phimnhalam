package com.aimovie.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchResultDTO {
    private String searchQuery;
    private List<MovieSearchDTO> movies;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int size;
    private boolean hasNext;
    private boolean hasPrevious;
    private List<String> suggestedGenres;
    private List<String> suggestedActors;
    private List<String> suggestedDirectors;
}
