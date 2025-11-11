package com.aimovie.controller;

import com.aimovie.dto.MovieSearchDTO;
import com.aimovie.service.UserFeatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MovieStatusController {

    private final UserFeatureService userFeatureService;

    @GetMapping("/movies/featured")
    public ResponseEntity<List<MovieSearchDTO>> getFeaturedMovies(@RequestParam(defaultValue = "10") int limit) {
        List<MovieSearchDTO> movies = userFeatureService.getFeaturedMovies(null, limit);
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/movies/trending")
    public ResponseEntity<List<MovieSearchDTO>> getTrendingMovies(@RequestParam(defaultValue = "10") int limit) {
        List<MovieSearchDTO> movies = userFeatureService.getTrendingMovies(null, limit);
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/movies/upcoming")
    public ResponseEntity<List<MovieSearchDTO>> getUpcomingMovies(@RequestParam(defaultValue = "10") int limit) {
        List<MovieSearchDTO> movies = userFeatureService.getUpcomingMovies(limit);
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/movies/now-showing")
    public ResponseEntity<List<MovieSearchDTO>> getNowShowingMovies(@RequestParam(defaultValue = "10") int limit) {
        List<MovieSearchDTO> movies = userFeatureService.getNowShowingMovies(limit);
        return ResponseEntity.ok(movies);
    }
}