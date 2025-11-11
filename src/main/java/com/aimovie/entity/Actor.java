package com.aimovie.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "actors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper=false, exclude = {"movies"})
@ToString(exclude = {"movies"})
public class Actor extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 150)
    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Size(max = 500)
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "dob")
    private LocalDate dob;

    @Size(max = 2000)
    @Column(name = "description", length = 2000)
    private String description;

    @ManyToMany
    @JoinTable(
            name = "movie_actor_links",
            joinColumns = @JoinColumn(name = "actor_id"),
            inverseJoinColumns = @JoinColumn(name = "movie_id")
    )
    @Builder.Default
    private Set<Movie> movies = new HashSet<>();
}


