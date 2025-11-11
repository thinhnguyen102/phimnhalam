package com.aimovie.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "watch_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchHistory extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    @NotNull
    private Movie movie;

    @Column(name = "watch_duration_seconds")
    @Builder.Default
    private Integer watchDurationSeconds = 0;

    @Column(name = "total_duration_seconds")
    private Integer totalDurationSeconds;

    @Column(name = "watch_percentage")
    @Builder.Default
    private Double watchPercentage = 0.0;

    @Column(name = "last_watched_at")
    private java.time.LocalDateTime lastWatchedAt;

    @Column(name = "is_completed")
    @Builder.Default
    private Boolean isCompleted = false;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Column(name = "quality", length = 20)
    private String quality;

    @Column(name = "subtitle_language", length = 10)
    private String subtitleLanguage;

    @Column(name = "volume_level")
    @Builder.Default
    private Double volumeLevel = 1.0;

    @Column(name = "playback_speed")
    @Builder.Default
    private Double playbackSpeed = 1.0;

    public Integer getTotalDurationSeconds() {
        return totalDurationSeconds;
    }
}
