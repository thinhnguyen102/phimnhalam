package com.aimovie.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "directors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Director {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 100)
    @NotBlank(message = "Director name is required")
    @Size(max = 100, message = "Director name must not exceed 100 characters")
    private String name;
    
    @Column(name = "biography", columnDefinition = "TEXT")
    @Size(max = 2000, message = "Biography must not exceed 2000 characters")
    private String biography;
    
    @Column(name = "birth_date")
    private java.time.LocalDate birthDate;
    
    @Column(name = "nationality", length = 50)
    @Size(max = 50, message = "Nationality must not exceed 50 characters")
    private String nationality;
    
    @Column(name = "photo_url", length = 500)
    @Size(max = 500, message = "Photo URL must not exceed 500 characters")
    private String photoUrl;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Relationship with Movies
    @OneToMany(mappedBy = "director", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Movie> movies;
}
