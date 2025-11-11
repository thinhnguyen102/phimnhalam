package com.aimovie.service;

import com.aimovie.dto.RatingDTOs;
import com.aimovie.dto.PageResponse;
import com.aimovie.entity.Movie;
import com.aimovie.entity.Rating;
import com.aimovie.entity.User;
import com.aimovie.mapper.RatingMapper;
import com.aimovie.repository.MovieRepository;
import com.aimovie.repository.RatingRepository;
import com.aimovie.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    public RatingDTOs.RatingResponseDTO createRating(Long userId, RatingDTOs.RatingCreateDTO createDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Movie movie = movieRepository.findById(createDTO.getMovieId())
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        if (ratingRepository.existsByUserIdAndMovieId(userId, createDTO.getMovieId())) {
            throw new RuntimeException("User has already rated this movie");
        }

        Rating rating = RatingMapper.toEntity(createDTO, user, movie);
        Rating savedRating = ratingRepository.save(rating);
        
        // Update movie's average rating
        updateMovieAverageRating(createDTO.getMovieId());
        
        return RatingMapper.toResponse(savedRating);
    }

    public RatingDTOs.RatingResponseDTO updateRating(Long userId, Long ratingId, RatingDTOs.RatingUpdateDTO updateDTO) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Rating not found"));

        if (!rating.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to update this rating");
        }

        RatingMapper.updateEntity(rating, updateDTO);
        Rating savedRating = ratingRepository.save(rating);
        
        // Update movie's average rating
        updateMovieAverageRating(rating.getMovie().getId());
        
        return RatingMapper.toResponse(savedRating);
    }

    public PageResponse<RatingDTOs.RatingResponseDTO> getRatingsByMovie(Long movieId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Rating> ratingPage = ratingRepository.findByMovieId(movieId, pageable);
        
        List<RatingDTOs.RatingResponseDTO> ratings = ratingPage.getContent()
                .stream()
                .map(RatingMapper::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<RatingDTOs.RatingResponseDTO>builder()
                .items(ratings)
                .page(ratingPage.getNumber())
                .size(ratingPage.getSize())
                .totalElements(ratingPage.getTotalElements())
                .totalPages(ratingPage.getTotalPages())
                .build();
    }

    public PageResponse<RatingDTOs.RatingResponseDTO> getRatingsByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Rating> ratingPage = ratingRepository.findByUserId(userId, pageable);
        
        List<RatingDTOs.RatingResponseDTO> ratings = ratingPage.getContent()
                .stream()
                .map(RatingMapper::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<RatingDTOs.RatingResponseDTO>builder()
                .items(ratings)
                .page(ratingPage.getNumber())
                .size(ratingPage.getSize())
                .totalElements(ratingPage.getTotalElements())
                .totalPages(ratingPage.getTotalPages())
                .build();
    }

    public RatingDTOs.RatingResponseDTO getUserRatingForMovie(Long userId, Long movieId) {
        Rating rating = ratingRepository.findByUserIdAndMovieId(userId, movieId)
                .orElseThrow(() -> new RuntimeException("Rating not found"));
        return RatingMapper.toResponse(rating);
    }

    public void deleteRating(Long userId, Long ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Rating not found"));

        if (!rating.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this rating");
        }

        Long movieId = rating.getMovie().getId();
        ratingRepository.delete(rating);
        
        // Update movie's average rating
        updateMovieAverageRating(movieId);
    }

    private void updateMovieAverageRating(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        
        List<Rating> ratings = ratingRepository.findByMovieId(movieId);
        
        if (ratings.isEmpty()) {
            movie.setAverageRating(0.0);
            movie.setTotalRatings(0L);
        } else {
            double average = ratings.stream()
                    .mapToInt(Rating::getStars)
                    .average()
                    .orElse(0.0);
            movie.setAverageRating(Math.round(average * 10.0) / 10.0); // Round to 1 decimal place
            movie.setTotalRatings((long) ratings.size());
        }
        
        movieRepository.save(movie);
    }
}
