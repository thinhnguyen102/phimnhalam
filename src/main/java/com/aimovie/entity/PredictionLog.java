package com.aimovie.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "prediction_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionLog extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", foreignKey = @ForeignKey(name = "fk_prediction_requester"))
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_asset_id", foreignKey = @ForeignKey(name = "fk_prediction_image_asset"))
    private ImageAsset imageAsset;

    @Size(max = 255)
    @Column(length = 255)
    private String topCharacterName;

    @Size(max = 255)
    @Column(length = 255)
    private String topMovieTitle;

    @Column
    private Double confidence;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String rawResponseJson;
}



