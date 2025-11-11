package com.aimovie.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchRequest {
    private String query;
    private List<String> actors;
    private List<String> directors;
    private Integer yearFrom;
    private Integer yearTo;
    private String country;
    private String language;
    private String ageRating;
    private Double minRating;
    private Double maxRating;
    private String sortBy; 
    private String sortOrder; 
    private String sortDirection; 
    private Integer page;
    private Integer size;
}
