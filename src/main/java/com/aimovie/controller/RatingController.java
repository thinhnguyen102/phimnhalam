package com.aimovie.controller;

import com.aimovie.dto.PageResponse;
import com.aimovie.dto.RatingDTOs;
import com.aimovie.service.RatingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    public ResponseEntity<RatingDTOs.RatingResponseDTO> createRating(
            HttpServletRequest request,
            @Valid @RequestBody RatingDTOs.RatingCreateDTO createDTO) {
        Long userId = (Long) request.getAttribute("userId");
        RatingDTOs.RatingResponseDTO response = ratingService.createRating(userId, createDTO);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RatingDTOs.RatingResponseDTO> updateRating(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @RequestBody RatingDTOs.RatingUpdateDTO updateDTO) {
        Long userId = (Long) request.getAttribute("userId");
        RatingDTOs.RatingResponseDTO response = ratingService.updateRating(userId, id, updateDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<PageResponse<RatingDTOs.RatingResponseDTO>> getRatingsByMovie(
            @PathVariable Long movieId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<RatingDTOs.RatingResponseDTO> response = ratingService.getRatingsByMovie(movieId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<PageResponse<RatingDTOs.RatingResponseDTO>> getRatingsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<RatingDTOs.RatingResponseDTO> response = ratingService.getRatingsByUser(userId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/movie/{movieId}")
    public ResponseEntity<RatingDTOs.RatingResponseDTO> getUserRatingForMovie(
            @PathVariable Long userId,
            @PathVariable Long movieId) {
        RatingDTOs.RatingResponseDTO response = ratingService.getUserRatingForMovie(userId, movieId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRating(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        ratingService.deleteRating(userId, id);
        return ResponseEntity.noContent().build();
    }
}
