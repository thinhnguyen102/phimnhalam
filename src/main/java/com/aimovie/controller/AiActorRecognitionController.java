package com.aimovie.controller;

import com.aimovie.dto.*;
import com.aimovie.service.AiActorRecognitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/ai/actor-recognition")
@RequiredArgsConstructor
@Slf4j
public class AiActorRecognitionController {

    private final AiActorRecognitionService aiActorRecognitionService;


    @PostMapping("/recognize")
    public ResponseEntity<AiActorRecognitionResponse> recognizeActorFromImage(
            @RequestParam("image") MultipartFile imageFile) {
        try {
            log.info("Received request to recognize actor from image: {}", imageFile.getOriginalFilename());
            
            if (imageFile.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            AiActorRecognitionResponse response = aiActorRecognitionService.recognizeActorFromImage(imageFile);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error recognizing actor from image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/recognize/base64")
    public ResponseEntity<AiActorRecognitionResponse> recognizeActorFromBase64(
            @Valid @RequestBody AiActorRecognitionRequest request) {
        try {
            log.info("Received request to recognize actor from base64 image");
            
            if (request.getImageBase64() == null || request.getImageBase64().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            AiActorRecognitionResponse response = aiActorRecognitionService.recognizeActorFromBase64(request.getImageBase64());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error recognizing actor from base64", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/recognize/url")
    public ResponseEntity<AiActorRecognitionResponse> recognizeActorFromUrl(
            @Valid @RequestBody AiActorRecognitionRequest request) {
        try {
            log.info("Received request to recognize actor from image URL: {}", request.getImageUrl());
            
            if (request.getImageUrl() == null || request.getImageUrl().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            AiActorRecognitionResponse response = aiActorRecognitionService.recognizeActorFromUrl(request.getImageUrl());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error recognizing actor from URL", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PostMapping("/recognize-and-movies")
    public ResponseEntity<ActorRecognitionResult> recognizeActorAndGetMovies(
            @RequestParam("image") MultipartFile imageFile) {
        try {
            log.info("Received request to recognize actor and get movies from image: {}", imageFile.getOriginalFilename());
            
            if (imageFile.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            ActorRecognitionResult result = aiActorRecognitionService.recognizeActorAndGetMovies(imageFile);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error recognizing actor and getting movies", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/recognize-and-movies/base64")
    public ResponseEntity<ActorRecognitionResult> recognizeActorAndGetMoviesFromBase64(
            @Valid @RequestBody AiActorRecognitionRequest request) {
        try {
            log.info("Received request to recognize actor and get movies from base64 image");
            
            if (request.getImageBase64() == null || request.getImageBase64().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            ActorRecognitionResult result = aiActorRecognitionService.recognizeActorAndGetMoviesFromBase64(request.getImageBase64());
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error recognizing actor and getting movies from base64", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/recognize-and-movies/url")
    public ResponseEntity<ActorRecognitionResult> recognizeActorAndGetMoviesFromUrl(
            @Valid @RequestBody AiActorRecognitionRequest request) {
        try {
            log.info("Received request to recognize actor and get movies from image URL: {}", request.getImageUrl());
            
            if (request.getImageUrl() == null || request.getImageUrl().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            ActorRecognitionResult result = aiActorRecognitionService.recognizeActorAndGetMoviesFromUrl(request.getImageUrl());
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error recognizing actor and getting movies from URL", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/actor/{actorName}/movies")
    public ResponseEntity<ActorMoviesResponse> getActorMovies(@PathVariable String actorName) {
        try {
            log.info("Received request to get movies for actor: {}", actorName);
            
            if (actorName == null || actorName.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            ActorMoviesResponse response = aiActorRecognitionService.getActorMovies(actorName);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting movies for actor: {}", actorName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/actor/{actorName}/movies/list")
    public ResponseEntity<List<MovieSearchDTO>> getActorMoviesList(
            @PathVariable String actorName,
            @RequestParam(defaultValue = "20") int limit) {
        try {
            log.info("Received request to get movies list for actor: {} with limit: {}", actorName, limit);
            
            if (actorName == null || actorName.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            List<MovieSearchDTO> movies = aiActorRecognitionService.getActorMoviesList(actorName, limit);
            return ResponseEntity.ok(movies);
            
        } catch (Exception e) {
            log.error("Error getting movies list for actor: {}", actorName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/actor/{actorName}/statistics")
    public ResponseEntity<ActorMoviesResponse> getActorStatistics(@PathVariable String actorName) {
        try {
            log.info("Received request to get statistics for actor: {}", actorName);
            
            if (actorName == null || actorName.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            ActorMoviesResponse response = aiActorRecognitionService.getActorStatistics(actorName);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting statistics for actor: {}", actorName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PostMapping("/recognize/batch")
    public ResponseEntity<List<ActorRecognitionResult>> recognizeMultipleActors(
            @RequestParam("images") List<MultipartFile> imageFiles) {
        try {
            log.info("Received request to recognize multiple actors from {} images", imageFiles.size());
            
            if (imageFiles.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            List<ActorRecognitionResult> results = aiActorRecognitionService.recognizeMultipleActors(imageFiles);
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            log.error("Error recognizing multiple actors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/recognize/batch/base64")
    public ResponseEntity<List<ActorRecognitionResult>> recognizeMultipleActorsFromBase64(
            @Valid @RequestBody List<AiActorRecognitionRequest> requests) {
        try {
            log.info("Received request to recognize multiple actors from {} base64 images", requests.size());
            
            if (requests.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            List<String> imageBase64List = requests.stream()
                    .map(AiActorRecognitionRequest::getImageBase64)
                    .collect(java.util.stream.Collectors.toList());
            
            List<ActorRecognitionResult> results = aiActorRecognitionService.recognizeMultipleActorsFromBase64(imageBase64List);
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            log.error("Error recognizing multiple actors from base64", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/actors/popular")
    public ResponseEntity<List<String>> getPopularActors(
            @RequestParam(defaultValue = "20") int limit) {
        try {
            log.info("Received request to get popular actors with limit: {}", limit);
            
            List<String> actors = aiActorRecognitionService.getPopularActors(limit);
            return ResponseEntity.ok(actors);
            
        } catch (Exception e) {
            log.error("Error getting popular actors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/actors/genre/{genre}")
    public ResponseEntity<List<String>> getActorsByGenre(
            @PathVariable String genre,
            @RequestParam(defaultValue = "20") int limit) {
        try {
            log.info("Received request to get actors by genre: {} with limit: {}", genre, limit);
            
            if (genre == null || genre.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            List<String> actors = aiActorRecognitionService.getActorsByGenre(genre, limit);
            return ResponseEntity.ok(actors);
            
        } catch (Exception e) {
            log.error("Error getting actors by genre: {}", genre, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/health")
    public ResponseEntity<AiServiceHealth> checkAiServiceHealth() {
        try {
            log.info("Received request to check AI service health");
            
            AiServiceHealth health = aiActorRecognitionService.checkAiServiceHealth();
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            log.error("Error checking AI service health", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/config")
    public ResponseEntity<AiServiceConfig> getAiServiceConfig() {
        try {
            log.info("Received request to get AI service config");
            
            AiServiceConfig config = aiActorRecognitionService.getAiServiceConfig();
            return ResponseEntity.ok(config);
            
        } catch (Exception e) {
            log.error("Error getting AI service config", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/config")
    public ResponseEntity<Void> updateAiServiceConfig(
            @Valid @RequestBody AiServiceConfig config) {
        try {
            log.info("Received request to update AI service config");
            
            aiActorRecognitionService.updateAiServiceConfig(config);
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("Error updating AI service config", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/demo/actors")
    public ResponseEntity<List<String>> getDemoActors() {
        try {
            List<String> demoActors = List.of(
                "Tom Hanks",
                "Leonardo DiCaprio",
                "Brad Pitt",
                "Scarlett Johansson",
                "Robert Downey Jr.",
                "Emma Stone",
                "Ryan Gosling",
                "Jennifer Lawrence",
                "Chris Evans",
                "Margot Robbie"
            );
            
            return ResponseEntity.ok(demoActors);
            
        } catch (Exception e) {
            log.error("Error getting demo actors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/demo/actor/{actorName}/movies")
    public ResponseEntity<ActorMoviesResponse> getDemoActorMovies(@PathVariable String actorName) {
        try {
            log.info("Received request for demo movies of actor: {}", actorName);
            
            ActorMoviesResponse demoResponse = ActorMoviesResponse.builder()
                    .actorName(actorName)
                    .movies(List.of())
                    .totalMovies(0L)
                    .averageRating(0.0)
                    .mostPopularGenre("Action")
                    .allGenres(List.of("Action", "Drama", "Comedy"))
                    .totalViewCount(0)
                    .build();
            
            return ResponseEntity.ok(demoResponse);
            
        } catch (Exception e) {
            log.error("Error getting demo actor movies", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
