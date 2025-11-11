package com.aimovie.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StreamingRequest {
    private Long movieId;
    private String quality; 
    private String subtitleLanguage;
    private Boolean subtitleEnabled;
    private Integer startPosition; 
    private Boolean autoPlay;
}
