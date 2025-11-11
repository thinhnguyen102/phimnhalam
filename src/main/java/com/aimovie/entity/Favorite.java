package com.aimovie.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "favorites",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_favorites_user_movie", columnNames = {"user_id", "movie_id"})
       }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Favorite extends Auditable {

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

    @Column(name = "is_favorite")
    @Builder.Default
    private Boolean isFavorite = true;

    @Column(name = "added_at")
    private java.time.LocalDateTime addedAt;
}
