package com.aimovie.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "video_resolutions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"movie_id", "quality"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class VideoResolution extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 20)
    @Column(name = "quality", length = 20, nullable = false)
    private String quality;

    @NotNull
    @Column(name = "width", nullable = false)
    private Integer width;

    @NotNull
    @Column(name = "height", nullable = false)
    private Integer height;

    @Size(max = 1000)
    @Column(name = "video_url", length = 1000)
    private String videoUrl;

    @Size(max = 100)
    @Column(name = "video_format", length = 100)
    private String videoFormat;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "bitrate")
    private Integer bitrate;

    @Column(name = "is_available")
    @Builder.Default
    private Boolean isAvailable = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Column(name = "encoding_status")
    @Builder.Default
    private String encodingStatus = "PENDING";

    @Column(name = "encoding_progress")
    @Builder.Default
    private Integer encodingProgress = 0;
}
