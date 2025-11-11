package com.aimovie.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "characters",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_characters_name", columnNames = {"name"})
       }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Character extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(length = 255, nullable = false)
    private String name;

    @ElementCollection
    @CollectionTable(name = "character_aliases", joinColumns = @JoinColumn(name = "character_id"))
    @Column(name = "alias", length = 255, nullable = false)
    @Builder.Default
    private List<String> aliases = new ArrayList<>();

    @Size(max = 500)
    @Column(length = 500)
    private String portraitUrl;
}



