package com.aimovie.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentModerationRequest {
    private Long commentId;
    private boolean approved;
    private boolean deleted;
    private String reason;
}
