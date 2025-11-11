package com.aimovie.mapper;

import com.aimovie.dto.RatingDTOs;
import com.aimovie.entity.Movie;
import com.aimovie.entity.Rating;
import com.aimovie.entity.User;

public class RatingMapper {

    public static Rating toEntity(RatingDTOs.RatingCreateDTO dto, User user, Movie movie) {
        if (dto == null) return null;
        return Rating.builder()
                .user(user)
                .movie(movie)
                .stars(dto.getStars())
                .comment(dto.getComment())
                .build();
    }

    public static void updateEntity(Rating entity, RatingDTOs.RatingUpdateDTO dto) {
        if (entity == null || dto == null) return;
        if (dto.getStars() != null) entity.setStars(dto.getStars());
        if (dto.getComment() != null) entity.setComment(dto.getComment());
    }

    public static RatingDTOs.RatingResponseDTO toResponse(Rating entity) {
        if (entity == null) return null;
        return RatingDTOs.RatingResponseDTO.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .movieId(entity.getMovie() != null ? entity.getMovie().getId() : null)
                .stars(entity.getStars())
                .comment(entity.getComment())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}



