package com.aimovie.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "ratings",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_rating_user_movie", columnNames = {"user_id", "movie_id"})
       }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_rating_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false, foreignKey = @ForeignKey(name = "fk_rating_movie"))
    private Movie movie;

    @NotNull
    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer stars;

    @Size(max = 1000)
    @Column(length = 1000)
    private String comment;
}



