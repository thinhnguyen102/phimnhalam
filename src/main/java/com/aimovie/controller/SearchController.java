package com.aimovie.controller;

import com.aimovie.dto.*;
import com.aimovie.service.UserFeatureService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final UserFeatureService userFeatureService;

    // ==================== MOVIE SEARCH ====================

    @PostMapping("/movies")
    public ResponseEntity<SearchResultDTO> searchMovies(
            @Valid @RequestBody SearchRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            SearchResultDTO results = userFeatureService.searchMovies(userId, request);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error searching movies", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/movies")
    public ResponseEntity<SearchResultDTO> searchMoviesByQuery(
            @RequestParam String query,
            @RequestParam(required = false) List<String> actors,
            @RequestParam(required = false) List<String> directors,
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String ageRating,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxRating,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @PageableDefault(size = 20) Pageable pageable,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query)
                    .actors(actors)
                    .directors(directors)
                    .yearFrom(yearFrom)
                    .yearTo(yearTo)
                    .country(country)
                    .language(language)
                    .ageRating(ageRating)
                    .minRating(minRating)
                    .maxRating(maxRating)
                    .sortBy(sortBy)
                    .sortDirection(sortDirection)
                    .page(pageable.getPageNumber())
                    .size(pageable.getPageSize())
                    .build();
            
            SearchResultDTO results = userFeatureService.searchMovies(userId, searchRequest);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error searching movies by query", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== QUICK SEARCH ====================

    @GetMapping("/quick")
    public ResponseEntity<List<MovieSearchDTO>> quickSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            // Allow null userId for public search
            
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query)
                    .size(limit)
                    .build();
            
            SearchResultDTO results = userFeatureService.searchMovies(userId, searchRequest);
            return ResponseEntity.ok(results.getMovies());
        } catch (Exception e) {
            log.error("Error in quick search", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getSearchSuggestions(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            // Implementation for search suggestions
            // This could be based on popular searches, movie titles, etc.
            List<String> suggestions = List.of();
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            log.error("Error getting search suggestions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== FILTERED SEARCH ====================

    @GetMapping("/movies/genre/{genre}")
    public ResponseEntity<List<MovieSearchDTO>> searchByGenre(
            @PathVariable String genre,
            @RequestParam(defaultValue = "20") int limit,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<MovieSearchDTO> movies = userFeatureService.getMoviesByGenre(userId, genre, limit);
            return ResponseEntity.ok(movies);
        } catch (Exception e) {
            log.error("Error searching by genre", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/movies/actor/{actor}")
    public ResponseEntity<List<MovieSearchDTO>> searchByActor(
            @PathVariable String actor,
            @RequestParam(defaultValue = "20") int limit,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            
            SearchRequest searchRequest = SearchRequest.builder()
                    .actors(List.of(actor))
                    .size(limit)
                    .build();
            
            SearchResultDTO results = userFeatureService.searchMovies(userId, searchRequest);
            return ResponseEntity.ok(results.getMovies());
        } catch (Exception e) {
            log.error("Error searching by actor", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/actor/{actor}")
    public ResponseEntity<List<MovieSearchDTO>> searchByActorShort(
            @PathVariable String actor,
            @RequestParam(defaultValue = "20") int limit,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            // Allow null userId for public search
            
            SearchRequest searchRequest = SearchRequest.builder()
                    .actors(List.of(actor))
                    .size(limit)
                    .build();
            
            SearchResultDTO results = userFeatureService.searchMovies(userId, searchRequest);
            return ResponseEntity.ok(results.getMovies());
        } catch (Exception e) {
            log.error("Error searching by actor", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/movies/director/{director}")
    public ResponseEntity<List<MovieSearchDTO>> searchByDirector(
            @PathVariable String director,
            @RequestParam(defaultValue = "20") int limit,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            
            SearchRequest searchRequest = SearchRequest.builder()
                    .directors(List.of(director))
                    .size(limit)
                    .build();
            
            SearchResultDTO results = userFeatureService.searchMovies(userId, searchRequest);
            return ResponseEntity.ok(results.getMovies());
        } catch (Exception e) {
            log.error("Error searching by director", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/director/{director}")
    public ResponseEntity<List<MovieSearchDTO>> searchByDirectorShort(
            @PathVariable String director,
            @RequestParam(defaultValue = "20") int limit,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            // Allow null userId for public search
            
            SearchRequest searchRequest = SearchRequest.builder()
                    .directors(List.of(director))
                    .size(limit)
                    .build();
            
            SearchResultDTO results = userFeatureService.searchMovies(userId, searchRequest);
            return ResponseEntity.ok(results.getMovies());
        } catch (Exception e) {
            log.error("Error searching by director", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/movies/year/{year}")
    public ResponseEntity<List<MovieSearchDTO>> searchByYear(
            @PathVariable Integer year,
            @RequestParam(defaultValue = "20") int limit,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            
            SearchRequest searchRequest = SearchRequest.builder()
                    .yearFrom(year)
                    .yearTo(year)
                    .size(limit)
                    .build();
            
            SearchResultDTO results = userFeatureService.searchMovies(userId, searchRequest);
            return ResponseEntity.ok(results.getMovies());
        } catch (Exception e) {
            log.error("Error searching by year", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/year/{year}")
    public ResponseEntity<List<MovieSearchDTO>> searchByYearShort(
            @PathVariable Integer year,
            @RequestParam(defaultValue = "20") int limit,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            // Allow null userId for public search
            
            SearchRequest searchRequest = SearchRequest.builder()
                    .yearFrom(year)
                    .yearTo(year)
                    .size(limit)
                    .build();
            
            SearchResultDTO results = userFeatureService.searchMovies(userId, searchRequest);
            return ResponseEntity.ok(results.getMovies());
        } catch (Exception e) {
            log.error("Error searching by year", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/movies/rating")
    public ResponseEntity<List<MovieSearchDTO>> searchByRating(
            @RequestParam Double minRating,
            @RequestParam(required = false) Double maxRating,
            @RequestParam(defaultValue = "20") int limit,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            
            SearchRequest searchRequest = SearchRequest.builder()
                    .minRating(minRating)
                    .maxRating(maxRating != null ? maxRating : 10.0)
                    .size(limit)
                    .build();
            
            SearchResultDTO results = userFeatureService.searchMovies(userId, searchRequest);
            return ResponseEntity.ok(results.getMovies());
        } catch (Exception e) {
            log.error("Error searching by rating", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== ADVANCED SEARCH ====================

    @PostMapping("/advanced")
    public ResponseEntity<SearchResultDTO> advancedSearch(
            @Valid @RequestBody SearchRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            SearchResultDTO results = userFeatureService.searchMovies(userId, request);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error in advanced search", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== SEARCH FACETS ====================

    @GetMapping("/facets/genres")
    public ResponseEntity<List<String>> getAvailableGenres() {
        try {
            // Genres removed, return empty list
            return ResponseEntity.ok(List.of());
        } catch (Exception e) {
            log.error("Error getting available genres", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/facets/actors")
    public ResponseEntity<List<String>> getPopularActors(
            @RequestParam(defaultValue = "50") int limit) {
        try {
            // Implementation for getting popular actors
            List<String> actors = List.of();
            return ResponseEntity.ok(actors);
        } catch (Exception e) {
            log.error("Error getting popular actors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/facets/directors")
    public ResponseEntity<List<String>> getPopularDirectors(
            @RequestParam(defaultValue = "50") int limit) {
        try {
            // Implementation for getting popular directors
            List<String> directors = List.of();
            return ResponseEntity.ok(directors);
        } catch (Exception e) {
            log.error("Error getting popular directors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/facets/countries")
    public ResponseEntity<List<String>> getAvailableCountries() {
        try {
            // Implementation for getting available countries
            List<String> countries = List.of("USA", "UK", "France", "Germany", "Japan", "South Korea", "India");
            return ResponseEntity.ok(countries);
        } catch (Exception e) {
            log.error("Error getting available countries", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/facets/languages")
    public ResponseEntity<List<String>> getAvailableLanguages() {
        try {
            // Implementation for getting available languages
            List<String> languages = List.of("English", "Vietnamese", "Chinese", "Japanese", "Korean", "French", "German");
            return ResponseEntity.ok(languages);
        } catch (Exception e) {
            log.error("Error getting available languages", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== SEARCH HISTORY ====================

    @GetMapping("/history")
    public ResponseEntity<List<String>> getSearchHistory(HttpServletRequest request) {
        try {
            // Implementation for getting user's search history
            List<String> history = List.of();
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error getting search history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/history")
    public ResponseEntity<Void> clearSearchHistory(HttpServletRequest request) {
        try {
            // Implementation for clearing user's search history
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error clearing search history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/history/{query}")
    public ResponseEntity<Void> removeFromSearchHistory(
            @PathVariable String query,
            HttpServletRequest request) {
        try {
            // Implementation for removing specific query from search history
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error removing from search history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== TRENDING SEARCHES ====================

    @GetMapping("/trending")
    public ResponseEntity<List<String>> getTrendingSearches(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<String> trending = List.of();
            return ResponseEntity.ok(trending);
        } catch (Exception e) {
            log.error("Error getting trending searches", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/popular")
    public ResponseEntity<List<String>> getPopularSearches(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            // Implementation for getting popular searches
            List<String> popular = List.of();
            return ResponseEntity.ok(popular);
        } catch (Exception e) {
            log.error("Error getting popular searches", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== SEARCH ANALYTICS ====================

    @PostMapping("/analytics")
    public ResponseEntity<Void> trackSearch(
            @RequestParam String query,
            @RequestParam(required = false) String filters,
            HttpServletRequest request) {
        try {
            // Implementation for tracking search analytics
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error tracking search", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
