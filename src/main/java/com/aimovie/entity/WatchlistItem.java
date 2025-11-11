package com.aimovie.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "watchlist_items",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_watchlist_user_movie", columnNames = {"user_id", "movie_id"})
       }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchlistItem extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_watchlist_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false, foreignKey = @ForeignKey(name = "fk_watchlist_movie"))
    private Movie movie;
}



