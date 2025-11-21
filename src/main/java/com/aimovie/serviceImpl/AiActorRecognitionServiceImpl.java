package com.aimovie.serviceImpl;

import com.aimovie.dto.*;
import com.aimovie.entity.Movie;
import com.aimovie.repository.MovieRepository;
import com.aimovie.service.AiActorRecognitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiActorRecognitionServiceImpl implements AiActorRecognitionService {

    private final MovieRepository movieRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ai.service.base-url:http://localhost:3000}")
    private String aiServiceBaseUrl;

    @Value("${ai.service.endpoint:/api/recognize-actor}")
    private String aiServiceEndpoint;

    @Value("${ai.service.timeout:30}")
    private Integer timeoutSeconds;

    @Value("${ai.service.confidence-threshold:0.7}")
    private Double defaultConfidenceThreshold;


    @Override
    public AiActorRecognitionResponse recognizeActorFromImage(MultipartFile imageFile) {
        try {
            log.info("Starting actor recognition from image file: {}", imageFile.getOriginalFilename());
            
            
            String imageBase64 = convertToBase64(imageFile);
            

            AiActorRecognitionRequest request = AiActorRecognitionRequest.builder()
                    .imageBase64(imageBase64)
                    .imageFormat(imageFile.getContentType())
                    .confidenceThreshold(defaultConfidenceThreshold)
                    .build();
            
            return callAiService(request);
            
        } catch (Exception e) {
            log.error("Error recognizing actor from image file", e);
            return createErrorResponse("Failed to recognize actor from image: " + e.getMessage());
        }
    }

    @Override
    public AiActorRecognitionResponse recognizeActorFromBase64(String imageBase64) {
        try {
            log.info("Starting actor recognition from base64 image");
            
            AiActorRecognitionRequest request = AiActorRecognitionRequest.builder()
                    .imageBase64(imageBase64)
                    .confidenceThreshold(defaultConfidenceThreshold)
                    .build();
            
            return callAiService(request);
            
        } catch (Exception e) {
            log.error("Error recognizing actor from base64", e);
            return createErrorResponse("Failed to recognize actor from base64: " + e.getMessage());
        }
    }

    @Override
    public AiActorRecognitionResponse recognizeActorFromUrl(String imageUrl) {
        try {
            log.info("Starting actor recognition from image URL: {}", imageUrl);
            
            AiActorRecognitionRequest request = AiActorRecognitionRequest.builder()
                    .imageUrl(imageUrl)
                    .confidenceThreshold(defaultConfidenceThreshold)
                    .build();
            
            return callAiService(request);
            
        } catch (Exception e) {
            log.error("Error recognizing actor from URL", e);
            return createErrorResponse("Failed to recognize actor from URL: " + e.getMessage());
        }
    }

    @Override
    public ActorMoviesResponse getActorMovies(String actorName) {
        try {
            log.info("Getting movies for actor: {}", actorName);
            
            List<Movie> movies = movieRepository.findAll()
                    .stream()
                    .filter(movie -> movie.getActors() != null && 
                                   movie.getActors().stream()
                                       .anyMatch(actor -> actor.toLowerCase().contains(actorName.toLowerCase())))
                    .collect(Collectors.toList());
            
            List<MovieSearchDTO> movieDTOs = movies.stream()
                    .map(this::convertToMovieSearchDTO)
                    .collect(Collectors.toList());
            
            double averageRating = movies.stream()
                    .mapToDouble(movie -> movie.getAverageRating() != null ? movie.getAverageRating() : 0.0)
                    .average()
                    .orElse(0.0);
            
            String mostPopularGenre = "Unknown";
            Set<String> allGenres = new HashSet<>();
            
            int totalViewCount = movies.stream()
                    .mapToInt(movie -> movie.getViewCount() != null ? movie.getViewCount().intValue() : 0)
                    .sum();
            
            return ActorMoviesResponse.builder()
                    .actorName(actorName)
                    .movies(movieDTOs)
                    .totalMovies((long) movies.size())
                    .averageRating(averageRating)
                    .mostPopularGenre(mostPopularGenre)
                    .allGenres(new ArrayList<>(allGenres))
                    .totalViewCount(totalViewCount)
                    .build();
            
        } catch (Exception e) {
            log.error("Error getting movies for actor: {}", actorName, e);
            return ActorMoviesResponse.builder()
                    .actorName(actorName)
                    .movies(List.of())
                    .totalMovies(0L)
                    .averageRating(0.0)
                    .mostPopularGenre("Unknown")
                    .allGenres(List.of())
                    .totalViewCount(0)
                    .build();
        }
    }

    @Override
    public List<MovieSearchDTO> getActorMoviesList(String actorName, int limit) {
        try {
            log.info("Getting movies list for actor: {} with limit: {}", actorName, limit);
            
            List<Movie> movies = movieRepository.findAll()
                    .stream()
                    .filter(movie -> movie.getActors() != null && 
                                   movie.getActors().stream()
                                       .anyMatch(actor -> actor.toLowerCase().contains(actorName.toLowerCase())))
                    .limit(limit)
                    .collect(Collectors.toList());
            
            return movies.stream()
                    .map(this::convertToMovieSearchDTO)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("Error getting movies list for actor: {}", actorName, e);
            return List.of();
        }
    }

    @Override
    public ActorRecognitionResult recognizeActorAndGetMovies(MultipartFile imageFile) {
        try {
            log.info("Starting combined actor recognition and movie search");
            
            AiActorRecognitionResponse recognitionResponse = recognizeActorFromImage(imageFile);
            
            if (!recognitionResponse.isSuccess() || recognitionResponse.getActors().isEmpty()) {
                return ActorRecognitionResult.builder()
                        .recognitionResponse(recognitionResponse)
                        .moviesResponse(null)
                        .hasMovies(false)
                        .errorMessage("No actors recognized from image")
                        .build();
            }
            
            ActorRecognition firstActor = recognitionResponse.getActors().get(0);
            String actorName = firstActor.getActorName();
            
            ActorMoviesResponse moviesResponse = getActorMovies(actorName);
            
            return ActorRecognitionResult.builder()
                    .recognitionResponse(recognitionResponse)
                    .moviesResponse(moviesResponse)
                    .hasMovies(moviesResponse.getTotalMovies() > 0)
                    .errorMessage(null)
                    .build();
            
        } catch (Exception e) {
            log.error("Error in combined actor recognition and movie search", e);
            return ActorRecognitionResult.builder()
                    .recognitionResponse(createErrorResponse("Failed to recognize actor: " + e.getMessage()))
                    .moviesResponse(null)
                    .hasMovies(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    @Override
    public ActorRecognitionResult recognizeActorAndGetMoviesFromBase64(String imageBase64) {
        try {
            log.info("Starting combined actor recognition and movie search from base64");
            
            AiActorRecognitionResponse recognitionResponse = recognizeActorFromBase64(imageBase64);
            
            if (!recognitionResponse.isSuccess() || recognitionResponse.getActors().isEmpty()) {
                return ActorRecognitionResult.builder()
                        .recognitionResponse(recognitionResponse)
                        .moviesResponse(null)
                        .hasMovies(false)
                        .errorMessage("No actors recognized from image")
                        .build();
            }
            
            ActorRecognition firstActor = recognitionResponse.getActors().get(0);
            String actorName = firstActor.getActorName();
            
            ActorMoviesResponse moviesResponse = getActorMovies(actorName);
            
            return ActorRecognitionResult.builder()
                    .recognitionResponse(recognitionResponse)
                    .moviesResponse(moviesResponse)
                    .hasMovies(moviesResponse.getTotalMovies() > 0)
                    .errorMessage(null)
                    .build();
            
        } catch (Exception e) {
            log.error("Error in combined actor recognition and movie search from base64", e);
            return ActorRecognitionResult.builder()
                    .recognitionResponse(createErrorResponse("Failed to recognize actor: " + e.getMessage()))
                    .moviesResponse(null)
                    .hasMovies(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    @Override
    public ActorRecognitionResult recognizeActorAndGetMoviesFromUrl(String imageUrl) {
        try {
            log.info("Starting combined actor recognition and movie search from URL");
            
            AiActorRecognitionResponse recognitionResponse = recognizeActorFromUrl(imageUrl);
            
            if (!recognitionResponse.isSuccess() || recognitionResponse.getActors().isEmpty()) {
                return ActorRecognitionResult.builder()
                        .recognitionResponse(recognitionResponse)
                        .moviesResponse(null)
                        .hasMovies(false)
                        .errorMessage("No actors recognized from image")
                        .build();
            }
            
            ActorRecognition firstActor = recognitionResponse.getActors().get(0);
            String actorName = firstActor.getActorName();
            
            ActorMoviesResponse moviesResponse = getActorMovies(actorName);
            
            return ActorRecognitionResult.builder()
                    .recognitionResponse(recognitionResponse)
                    .moviesResponse(moviesResponse)
                    .hasMovies(moviesResponse.getTotalMovies() > 0)
                    .errorMessage(null)
                    .build();
            
        } catch (Exception e) {
            log.error("Error in combined actor recognition and movie search from URL", e);
            return ActorRecognitionResult.builder()
                    .recognitionResponse(createErrorResponse("Failed to recognize actor: " + e.getMessage()))
                    .moviesResponse(null)
                    .hasMovies(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    @Override
    public AiServiceHealth checkAiServiceHealth() {
        try {
            log.info("Checking AI service health");
            
            long startTime = System.currentTimeMillis();
            
            String healthUrl = aiServiceBaseUrl + "/health";
            ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            boolean isHealthy = response.getStatusCode().is2xxSuccessful();
            
            return AiServiceHealth.builder()
                    .isHealthy(isHealthy)
                    .status(isHealthy ? "UP" : "DOWN")
                    .version("1.0.0")
                    .responseTime(responseTime)
                    .lastChecked(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .errorMessage(isHealthy ? null : "Service returned status: " + response.getStatusCode())
                    .build();
            
        } catch (Exception e) {
            log.error("Error checking AI service health", e);
            return AiServiceHealth.builder()
                    .isHealthy(false)
                    .status("DOWN")
                    .version("Unknown")
                    .responseTime(0L)
                    .lastChecked(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .errorMessage("Service unavailable: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public AiServiceConfig getAiServiceConfig() {
        return AiServiceConfig.builder()
                .baseUrl(aiServiceBaseUrl)
                .endpoint(aiServiceEndpoint)
                .timeoutSeconds(timeoutSeconds)
                .apiKey("N/A")
                .modelVersion("1.0.0")
                .defaultConfidenceThreshold(defaultConfidenceThreshold)
                .build();
    }

    @Override
    public void updateAiServiceConfig(AiServiceConfig config) {

        log.info("Updating AI service config: {}", config);
    }


    @Override
    public List<ActorRecognitionResult> recognizeMultipleActors(List<MultipartFile> imageFiles) {
        return imageFiles.stream()
                .map(this::recognizeActorAndGetMovies)
                .collect(Collectors.toList());
    }

    @Override
    public List<ActorRecognitionResult> recognizeMultipleActorsFromBase64(List<String> imageBase64List) {
        return imageBase64List.stream()
                .map(this::recognizeActorAndGetMoviesFromBase64)
                .collect(Collectors.toList());
    }


    @Override
    public ActorMoviesResponse getActorStatistics(String actorName) {
        return getActorMovies(actorName);
    }

    @Override
    public List<String> getPopularActors(int limit) {
        try {
            return movieRepository.findAll()
                    .stream()
                    .flatMap(movie -> movie.getActors().stream())
                    .collect(Collectors.groupingBy(actor -> actor, Collectors.counting()))
                    .entrySet()
                    .stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(limit)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting popular actors", e);
            return List.of();
        }
    }

    @Override
    public List<String> getActorsByGenre(String genre, int limit) {
        try {
            // Genres removed, return empty list
            return List.of();
        } catch (Exception e) {
            log.error("Error getting actors by genre: {}", genre, e);
            return List.of();
        }
    }


    private AiActorRecognitionResponse callAiService(AiActorRecognitionRequest request) {
        try {
            String url = aiServiceBaseUrl + aiServiceEndpoint;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<AiActorRecognitionRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<AiActorRecognitionResponse> response = restTemplate.postForEntity(
                    url, entity, AiActorRecognitionResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                return createErrorResponse("AI service returned error status: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Error calling AI service", e);
            return createErrorResponse("Failed to call AI service: " + e.getMessage());
        }
    }

    private String convertToBase64(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        return Base64.getEncoder().encodeToString(bytes);
    }

    private AiActorRecognitionResponse createErrorResponse(String message) {
        return AiActorRecognitionResponse.builder()
                .success(false)
                .message(message)
                .actors(List.of())
                .processingTime(0.0)
                .modelVersion("Unknown")
                .build();
    }

    private MovieSearchDTO convertToMovieSearchDTO(Movie movie) {
        return MovieSearchDTO.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .synopsis(movie.getSynopsis())
                .year(movie.getYear())
                .actors(movie.getActors())
                .directorName(movie.getDirector() != null ? movie.getDirector().getName() : null)
                .country(movie.getCountry() != null ? movie.getCountry().getName() : null)
                .language(movie.getLanguage())
                .ageRating(movie.getAgeRating())
                .imdbRating(movie.getImdbRating())
                .averageRating(movie.getAverageRating())
                .viewCount(movie.getViewCount())
                .posterUrl(movie.getPosterUrl())
                .thumbnailUrl(movie.getThumbnailUrl())
                .trailerUrl(movie.getTrailerUrl())
                .isFeatured(movie.getIsFeatured())
                .isTrending(movie.getIsTrending())
                .releaseDate(movie.getReleaseDate())
                .isInWatchlist(false)
                .isFavorite(false)
                .build();
    }
}
