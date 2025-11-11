package com.aimovie.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "watchlists",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_watchlists_collection_movie", columnNames = {"watchlist_collection_id", "movie_id"})
       }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Watchlist extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "watchlist_collection_id", nullable = false)
    @NotNull
    private WatchlistCollection watchlistCollection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    @NotNull
    private Movie movie;

    @Column(name = "is_in_watchlist")
    @Builder.Default
    private Boolean isInWatchlist = true;

    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "added_at")
    private java.time.LocalDateTime addedAt;
}
