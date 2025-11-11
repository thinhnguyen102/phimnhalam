package com.aimovie.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DownloadRequest {
    private Long movieId;
    private String quality; 
    private String format; 
    private String subtitleLanguage;
    private Boolean includeSubtitles;
}
