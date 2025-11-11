package com.aimovie.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "image_assets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageAsset extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", foreignKey = @ForeignKey(name = "fk_image_asset_owner"))
    private User owner;

    @NotBlank
    @Size(max = 1000)
    @Column(length = 1000, nullable = false)
    private String url;

    @NotBlank
    @Size(max = 100)
    @Column(length = 100, nullable = false)
    private String mediaType;

    @Column
    private Long sizeBytes;
}



