package com.aimovie.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "appearances",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_appearance_movie_character", columnNames = {"movie_id", "character_id"})
       }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appearance extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false, foreignKey = @ForeignKey(name = "fk_appearance_movie"))
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "character_id", nullable = false, foreignKey = @ForeignKey(name = "fk_appearance_character"))
    private Character character;

    @Size(max = 255)
    @Column(name = "role_name", length = 255)
    private String roleName;

    @Column(name = "billing_order")
    private Integer billingOrder;
}



