package com.aimovie.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCommentDTO {
    private Long id;
    private String content;
    private String username;
    private String movieTitle;
    private Long movieId;
    private Long parentCommentId;
    private Boolean isApproved;
    private Boolean isDeleted;
    private Long likeCount;
    private Long dislikeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
