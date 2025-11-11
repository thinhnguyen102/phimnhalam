package com.aimovie.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 1000)
    @Column(length = 1000, nullable = false)
    private String reason;

    @Size(max = 2000)
    @Column(length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id")
    private User reportedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_comment_id")
    private Comment reportedComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_movie_id")
    private Movie reportedMovie;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 50)
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Size(max = 1000)
    @Column(name = "resolution_note", length = 1000)
    private String resolutionNote;

    public enum ReportType {
        SPAM,
        HARASSMENT,
        INAPPROPRIATE_CONTENT,
        COPYRIGHT_VIOLATION,
        FAKE_INFORMATION,
        OTHER
    }

    public enum ReportStatus {
        PENDING,
        UNDER_REVIEW,
        RESOLVED,
        DISMISSED
    }
}
