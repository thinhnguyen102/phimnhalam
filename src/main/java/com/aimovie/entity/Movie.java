package com.aimovie.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "movies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper=false, exclude = {"categories"})
@ToString(exclude = {"categories"})
public class Movie extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(length = 255, nullable = false)
    private String title;

    @Size(max = 2000)
    @Column(length = 2000)
    private String synopsis;

    @Min(1888)
    @Max(2100)
    @Column
    private Integer year;

    @Size(max = 500)
    @Column(length = 500)
    private String posterUrl;

    @Size(max = 500)
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    
    @Size(max = 1000)
    @Column(name = "video_url", length = 1000)
    private String videoUrl;

    @Size(max = 100)
    @Column(name = "video_format", length = 100)
    private String videoFormat; 

    @Column(name = "video_duration")
    private Integer videoDuration; // Duration in seconds 

    @Size(max = 100)
    @Column(name = "video_quality", length = 100)
    private String videoQuality; 

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Size(max = 1000)
    @Column(name = "streaming_url", length = 1000)
    private String streamingUrl; 

    @Column(name = "is_available")
    @Builder.Default
    private Boolean isAvailable = true;

    @ElementCollection
    @CollectionTable(name = "movie_qualities", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "quality", length = 20, nullable = false)
    @Builder.Default
    private List<String> availableQualities = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "movie_actors", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "actor", length = 100, nullable = false)
    @Builder.Default
    private List<String> actors = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "director_id", nullable = false)
    @NotNull
    private Director director;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @Size(max = 100)
    @Column(name = "language", length = 100)
    private String language;

    @Column(name = "age_rating", length = 10)
    private String ageRating;

    @Column(name = "imdb_rating")
    private Double imdbRating;

    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L;

    @Column(name = "like_count")
    @Builder.Default
    private Long likeCount = 0L;

    @Column(name = "dislike_count")
    @Builder.Default
    private Long dislikeCount = 0L;

    @Column(name = "average_rating")
    @Builder.Default
    private Double averageRating = 0.0;

    @Column(name = "total_ratings")
    @Builder.Default
    private Long totalRatings = 0L;

    @Column(name = "comment_count")
    @Builder.Default
    private Long commentCount = 0L;

    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "is_trending")
    @Builder.Default
    private Boolean isTrending = false;

    @Column(name = "release_date")
    private java.time.LocalDate releaseDate;

    @Size(max = 1000)
    @Column(name = "trailer_url", length = 1000)
    private String trailerUrl;

    @Column(name = "download_enabled")
    @Builder.Default
    private Boolean downloadEnabled = false;

    @Column(name = "max_download_quality", length = 20)
    private String maxDownloadQuality; 

    @ManyToMany
    @JoinTable(
            name = "movie_categories",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

    @Column(name = "status", length = 50)
    private String status;
}



