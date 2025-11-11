package com.aimovie.controller;

import com.aimovie.entity.Director;
import com.aimovie.entity.Movie;
import com.aimovie.repository.DirectorRepository;
import com.aimovie.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/data-fix")
@RequiredArgsConstructor
@Slf4j
public class DataFixController {

    private final MovieRepository movieRepository;
    private final DirectorRepository directorRepository;

    @GetMapping("/check-director-issues")
    public ResponseEntity<Map<String, Object>> checkDirectorIssues() {
        try {
            // Find movies with director_id = 0 or NULL
            List<Movie> moviesWithIssues = movieRepository.findAll().stream()
                    .filter(movie -> movie.getDirector() == null || 
                            (movie.getDirector().getId() != null && movie.getDirector().getId() == 0))
                    .toList();

            // Get all directors
            List<Director> allDirectors = directorRepository.findAll();

            Map<String, Object> result = new HashMap<>();
            result.put("moviesWithDirectorIssues", moviesWithIssues.size());
            result.put("totalDirectors", allDirectors.size());
            result.put("directors", allDirectors.stream()
                    .map(d -> Map.of("id", d.getId(), "name", d.getName()))
                    .toList());
            result.put("problematicMovies", moviesWithIssues.stream()
                    .map(m -> Map.of("id", m.getId(), "title", m.getTitle(), 
                            "directorId", m.getDirector() != null ? m.getDirector().getId() : "NULL"))
                    .toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error checking director issues", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/fix-director-issues")
    public ResponseEntity<Map<String, Object>> fixDirectorIssues() {
        try {
            // Get first available director
            List<Director> directors = directorRepository.findAll();
            if (directors.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "No directors found in database");
                return ResponseEntity.badRequest().body(error);
            }

            Director defaultDirector = directors.get(0);
            log.info("Using default director: {} (ID: {})", defaultDirector.getName(), defaultDirector.getId());

            // Find and fix movies with director issues
            List<Movie> moviesWithIssues = movieRepository.findAll().stream()
                    .filter(movie -> movie.getDirector() == null || 
                            (movie.getDirector().getId() != null && movie.getDirector().getId() == 0))
                    .toList();

            int fixedCount = 0;
            for (Movie movie : moviesWithIssues) {
                movie.setDirector(defaultDirector);
                movieRepository.save(movie);
                fixedCount++;
                log.info("Fixed movie: {} (ID: {})", movie.getTitle(), movie.getId());
            }

            Map<String, Object> result = new HashMap<>();
            result.put("fixedMovies", fixedCount);
            result.put("defaultDirector", Map.of("id", defaultDirector.getId(), "name", defaultDirector.getName()));
            result.put("message", "Director issues fixed successfully");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error fixing director issues", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
