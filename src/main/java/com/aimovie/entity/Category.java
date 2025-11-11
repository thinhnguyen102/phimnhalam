package com.aimovie.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categories", uniqueConstraints = {
        @UniqueConstraint(name = "uk_category_name", columnNames = {"name"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper=false, exclude = {"movies"})
@ToString(exclude = {"movies"})
public class Category extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Size(max = 150)
    @Column(name = "display_name", length = 150)
    private String displayName;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @Size(max = 200)
    @Column(name = "icon", length = 200)
    private String icon;

    @ManyToMany(mappedBy = "categories")
    @Builder.Default
    private Set<Movie> movies = new HashSet<>();
}


