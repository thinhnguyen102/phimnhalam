package com.aimovie.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "watchlist_collections",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_watchlist_collections_user_name", columnNames = {"user_id", "name"})
       }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class WatchlistCollection extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = false;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "movie_count")
    @Builder.Default
    private Integer movieCount = 0;

    @OneToMany(mappedBy = "watchlistCollection", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Watchlist> watchlistItems = new ArrayList<>();

    @Column(name = "color_code", length = 7)
    private String colorCode; // Hex color for UI display

    @Column(name = "icon", length = 50)
    private String icon; // Icon name for UI display
}
