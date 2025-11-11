package com.aimovie.service;

import com.aimovie.dto.MovieDTOs;
import com.aimovie.dto.PageResponse;
import com.aimovie.dto.ActorCRUD;
import com.aimovie.entity.Movie;
import com.aimovie.mapper.MovieMapper;
import com.aimovie.repository.CategoryRepository;
import com.aimovie.repository.MovieRepository;
import com.aimovie.repository.ActorRepository;
import com.aimovie.repository.DirectorRepository;
import com.aimovie.repository.RatingRepository;
import com.aimovie.repository.CommentRepository;
import com.aimovie.repository.WatchlistRepository;
import com.aimovie.repository.FavoriteRepository;
import com.aimovie.repository.WatchHistoryRepository;
import com.aimovie.repository.VideoResolutionRepository;
import com.aimovie.repository.SubtitleRepository;
import com.aimovie.repository.AppearanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MovieService {

    private final MovieRepository movieRepository;
    private final CategoryRepository categoryRepository;
    private final FileUploadService fileUploadService;
    private final ActorRepository actorRepository;
    private final DirectorRepository directorRepository;
    private final RatingRepository ratingRepository;
    private final CommentRepository commentRepository;
    private final WatchlistRepository watchlistRepository;
    private final FavoriteRepository favoriteRepository;
    private final WatchHistoryRepository watchHistoryRepository;
    private final VideoResolutionRepository videoResolutionRepository;
    private final SubtitleRepository subtitleRepository;
    private final AppearanceRepository appearanceRepository;

    public MovieDTOs.MovieResponseDTO createMovie(MovieDTOs.MovieCreateDTO createDTO) {
        validateActorsExist(createDTO.getActors());
        validateDirectorExistsByName(createDTO.getDirectorName());
        Movie movie = MovieMapper.toEntity(createDTO);
        if (createDTO.getCategories() != null && !createDTO.getCategories().isEmpty()) {
            var namesLower = createDTO.getCategories().stream().map(String::trim).map(String::toLowerCase).toList();
            validateCategoriesExist(namesLower);
            var cats = categoryRepository.findByNameLowerIn(namesLower);
            movie.setCategories(new java.util.HashSet<>(cats));
        }
        Movie savedMovie = movieRepository.save(movie);
        MovieDTOs.MovieResponseDTO res = MovieMapper.toResponse(savedMovie);
        enrichActors(res, savedMovie.getActors());
        return res;
    }

    public MovieDTOs.MovieResponseDTO updateMovie(Long movieId, MovieDTOs.MovieUpdateDTO updateDTO) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        
        if (updateDTO.getActors() != null) {
            validateActorsExist(updateDTO.getActors());
        }
        if (updateDTO.getDirectorName() != null) {
            validateDirectorExistsByName(updateDTO.getDirectorName());
        }
        MovieMapper.updateEntity(movie, updateDTO);
        if (updateDTO.getCategories() != null) {
            var namesLower = updateDTO.getCategories().stream().map(String::trim).map(String::toLowerCase).toList();
            validateCategoriesExist(namesLower);
            var cats = categoryRepository.findByNameLowerIn(namesLower);
            movie.setCategories(new java.util.HashSet<>(cats));
        }
        Movie savedMovie = movieRepository.save(movie);
        MovieDTOs.MovieResponseDTO res = MovieMapper.toResponse(savedMovie);
        enrichActors(res, savedMovie.getActors());
        return res;
    }

    public MovieDTOs.MovieResponseDTO getMovieById(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        MovieDTOs.MovieResponseDTO res = MovieMapper.toResponse(movie);
        enrichActors(res, movie.getActors());
        return res;
    }

    public PageResponse<MovieDTOs.MovieResponseDTO> searchMovies(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Movie> moviePage = movieRepository.findByTitleContainingIgnoreCaseAndReleased(query, java.time.LocalDate.now(), pageable);
        
        List<MovieDTOs.MovieResponseDTO> movies = moviePage.getContent()
                .stream()
                .map(m -> {
                    MovieDTOs.MovieResponseDTO dto = MovieMapper.toResponse(m);
                    enrichActors(dto, m.getActors());
                    return dto;
                })
                .collect(Collectors.toList());

        return PageResponse.<MovieDTOs.MovieResponseDTO>builder()
                .items(movies)
                .page(moviePage.getNumber())
                .size(moviePage.getSize())
                .totalElements(moviePage.getTotalElements())
                .totalPages(moviePage.getTotalPages())
                .build();
    }

    public List<MovieDTOs.MovieResponseDTO> getAllMovies() {
        return movieRepository.findAll()
                .stream()
                .map(m -> {
                    MovieDTOs.MovieResponseDTO dto = MovieMapper.toResponse(m);
                    enrichActors(dto, m.getActors());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public void deleteMovie(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        
        log.info("Deleting movie with id: {} and all related data", movieId);
        
        ratingRepository.deleteByMovieId(movieId);
        commentRepository.deleteByMovie(movie);
        watchlistRepository.deleteByMovie(movie);
        favoriteRepository.deleteByMovie(movie);
        watchHistoryRepository.deleteByMovie(movie);
        videoResolutionRepository.deleteByMovie(movie);
        subtitleRepository.deleteByMovie(movie);
        appearanceRepository.deleteByMovieId(movieId);
        
        try {
            java.nio.file.Path movieVideoDir = java.nio.file.Paths.get("uploads", "videos", String.valueOf(movieId));
            if (java.nio.file.Files.exists(movieVideoDir)) {
                java.nio.file.Files.walk(movieVideoDir)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            java.nio.file.Files.delete(path);
                            log.info("Deleted file: {}", path);
                        } catch (IOException e) {
                            log.warn("Failed to delete file {}: {}", path, e.getMessage());
                        }
                    });
            }
        } catch (IOException e) {
            log.warn("Failed to delete video resolution directory for movie {}: {}", movieId, e.getMessage());
        }
        
        if (movie.getVideoUrl() != null && !movie.getVideoUrl().isEmpty()) {
            try {
                String filename = extractFilenameFromUrl(movie.getVideoUrl());
                fileUploadService.deleteVideoFile(filename);
            } catch (IOException e) {
                log.warn("Failed to delete video file for movie {}: {}", movieId, e.getMessage());
            }
        }
        
        if (movie.getPosterUrl() != null && !movie.getPosterUrl().isEmpty()) {
            try {
                String filename = extractFilenameFromUrl(movie.getPosterUrl());
                fileUploadService.deleteImageFile(filename);
            } catch (IOException e) {
                log.warn("Failed to delete poster file for movie {}: {}", movieId, e.getMessage());
            }
        }
        
        if (movie.getThumbnailUrl() != null && !movie.getThumbnailUrl().isEmpty()) {
            try {
                String filename = extractFilenameFromUrl(movie.getThumbnailUrl());
                fileUploadService.deleteImageFile(filename);
            } catch (IOException e) {
                log.warn("Failed to delete thumbnail file for movie {}: {}", movieId, e.getMessage());
            }
        }
        
        movieRepository.deleteById(movieId);
        log.info("Successfully deleted movie with id: {} and all related data", movieId);
    }

    public PageResponse<MovieDTOs.MovieResponseDTO> getMoviesByGenre(String genre, int page, int size) {
        // Genres removed, return empty page
        Page<Movie> moviePage = Page.empty();
        
        List<MovieDTOs.MovieResponseDTO> movies = moviePage.getContent()
                .stream()
                .map(MovieMapper::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<MovieDTOs.MovieResponseDTO>builder()
                .items(movies)
                .page(moviePage.getNumber())
                .size(moviePage.getSize())
                .totalElements(moviePage.getTotalElements())
                .totalPages(moviePage.getTotalPages())
                .build();
    }

    public PageResponse<MovieDTOs.MovieResponseDTO> getMoviesByYear(Integer year, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("releaseDate").descending().and(Sort.by("createdAt").descending()));
        Page<Movie> moviePage = movieRepository.findByYearAndReleased(year, java.time.LocalDate.now(), pageable);
        
        List<MovieDTOs.MovieResponseDTO> movies = moviePage.getContent()
                .stream()
                .map(m -> {
                    MovieDTOs.MovieResponseDTO dto = MovieMapper.toResponse(m);
                    enrichActors(dto, m.getActors());
                    return dto;
                })
                .collect(Collectors.toList());

        return PageResponse.<MovieDTOs.MovieResponseDTO>builder()
                .items(movies)
                .page(moviePage.getNumber())
                .size(moviePage.getSize())
                .totalElements(moviePage.getTotalElements())
                .totalPages(moviePage.getTotalPages())
                .build();
    }

    public PageResponse<MovieDTOs.MovieResponseDTO> getMoviesByYearRange(Integer startYear, Integer endYear, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("year").descending());
        Page<Movie> moviePage = movieRepository.findByYearBetweenAndReleased(startYear, endYear, java.time.LocalDate.now(), pageable);
        
        List<MovieDTOs.MovieResponseDTO> movies = moviePage.getContent()
                .stream()
                .map(m -> {
                    MovieDTOs.MovieResponseDTO dto = MovieMapper.toResponse(m);
                    enrichActors(dto, m.getActors());
                    return dto;
                })
                .collect(Collectors.toList());

        return PageResponse.<MovieDTOs.MovieResponseDTO>builder()
                .items(movies)
                .page(moviePage.getNumber())
                .size(moviePage.getSize())
                .totalElements(moviePage.getTotalElements())
                .totalPages(moviePage.getTotalPages())
                .build();
    }

    public PageResponse<MovieDTOs.MovieResponseDTO> getAvailableMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("releaseDate").descending().and(Sort.by("createdAt").descending()));
        Page<Movie> moviePage = movieRepository.findByIsAvailableTrueAndReleased(java.time.LocalDate.now(), pageable);
        
        List<MovieDTOs.MovieResponseDTO> movies = moviePage.getContent()
                .stream()
                .map(m -> {
                    MovieDTOs.MovieResponseDTO dto = MovieMapper.toResponse(m);
                    enrichActors(dto, m.getActors());
                    return dto;
                })
                .collect(Collectors.toList());

        return PageResponse.<MovieDTOs.MovieResponseDTO>builder()
                .items(movies)
                .page(moviePage.getNumber())
                .size(moviePage.getSize())
                .totalElements(moviePage.getTotalElements())
                .totalPages(moviePage.getTotalPages())
                .build();
    }

    public PageResponse<MovieDTOs.MovieResponseDTO> getUnavailableMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Movie> moviePage = movieRepository.findByIsAvailableFalse(pageable);
        
        List<MovieDTOs.MovieResponseDTO> movies = moviePage.getContent()
                .stream()
                .map(m -> {
                    MovieDTOs.MovieResponseDTO dto = MovieMapper.toResponse(m);
                    enrichActors(dto, m.getActors());
                    return dto;
                })
                .collect(Collectors.toList());

        return PageResponse.<MovieDTOs.MovieResponseDTO>builder()
                .items(movies)
                .page(moviePage.getNumber())
                .size(moviePage.getSize())
                .totalElements(moviePage.getTotalElements())
                .totalPages(moviePage.getTotalPages())
                .build();
    }

    public PageResponse<MovieDTOs.MovieResponseDTO> getLatestMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("releaseDate").descending().and(Sort.by("createdAt").descending()));
        Page<Movie> moviePage = movieRepository.findByIsAvailableTrueAndReleased(java.time.LocalDate.now(), pageable);
        
        List<MovieDTOs.MovieResponseDTO> movies = moviePage.getContent()
                .stream()
                .map(m -> {
                    MovieDTOs.MovieResponseDTO dto = MovieMapper.toResponse(m);
                    enrichActors(dto, m.getActors());
                    return dto;
                })
                .collect(Collectors.toList());

        return PageResponse.<MovieDTOs.MovieResponseDTO>builder()
                .items(movies)
                .page(moviePage.getNumber())
                .size(moviePage.getSize())
                .totalElements(moviePage.getTotalElements())
                .totalPages(moviePage.getTotalPages())
                .build();
    }

    public PageResponse<MovieDTOs.MovieResponseDTO> advancedSearch(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("releaseDate").descending().and(Sort.by("createdAt").descending()));
        Page<Movie> moviePage = movieRepository.findByTitleOrSynopsisContainingIgnoreCaseAndReleased(query, query, java.time.LocalDate.now(), pageable);
        
        List<MovieDTOs.MovieResponseDTO> movies = moviePage.getContent()
                .stream()
                .map(m -> {
                    MovieDTOs.MovieResponseDTO dto = MovieMapper.toResponse(m);
                    enrichActors(dto, m.getActors());
                    return dto;
                })
                .collect(Collectors.toList());

        return PageResponse.<MovieDTOs.MovieResponseDTO>builder()
                .items(movies)
                .page(moviePage.getNumber())
                .size(moviePage.getSize())
                .totalElements(moviePage.getTotalElements())
                .totalPages(moviePage.getTotalPages())
                .build();
    }

    public MovieDTOs.MovieResponseDTO toggleAvailability(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        
        movie.setIsAvailable(!movie.getIsAvailable());
        Movie savedMovie = movieRepository.save(movie);
        MovieDTOs.MovieResponseDTO res = MovieMapper.toResponse(savedMovie);
        enrichActors(res, savedMovie.getActors());
        return res;
    }

    public List<String> getAllGenres() {
        // Genres removed, return empty list
        return List.of();
    }

    public List<Integer> getAllYears() {
        return movieRepository.findAllDistinctYears();
    }

    public MovieDTOs.MovieResponseDTO uploadPoster(Long movieId, MultipartFile file) throws IOException {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        // Delete old poster if exists
        if (movie.getPosterUrl() != null && !movie.getPosterUrl().isEmpty()) {
            try {
                String oldFilename = extractFilenameFromUrl(movie.getPosterUrl());
                fileUploadService.deleteImageFile(oldFilename);
            } catch (IOException e) {
                log.warn("Failed to delete old poster file for movie {}: {}", movieId, e.getMessage());
            }
        }

        // Upload new poster
        String filename = fileUploadService.uploadImageFile(file);
        String posterUrl = fileUploadService.buildPublicImageUrl(filename);
        
        movie.setPosterUrl(posterUrl);
        Movie savedMovie = movieRepository.save(movie);
        
        MovieDTOs.MovieResponseDTO res = MovieMapper.toResponse(savedMovie);
        enrichActors(res, savedMovie.getActors());
        return res;
    }

    public MovieDTOs.MovieResponseDTO uploadThumbnail(Long movieId, MultipartFile file) throws IOException {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        if (movie.getThumbnailUrl() != null && !movie.getThumbnailUrl().isEmpty()) {
            try {
                String oldFilename = extractFilenameFromUrl(movie.getThumbnailUrl());
                fileUploadService.deleteImageFile(oldFilename);
            } catch (IOException e) {
                log.warn("Failed to delete old thumbnail file for movie {}: {}", movieId, e.getMessage());
            }
        }

        String filename = fileUploadService.uploadImageFile(file);
        String thumbnailUrl = fileUploadService.buildPublicImageUrl(filename);

        movie.setThumbnailUrl(thumbnailUrl);
        Movie savedMovie = movieRepository.save(movie);

        return MovieMapper.toResponse(savedMovie);
    }

    private String extractFilenameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        return url.substring(url.lastIndexOf("/") + 1);
    }

    private void enrichActors(MovieDTOs.MovieResponseDTO dto, List<String> actorNames) {
        if (actorNames == null || actorNames.isEmpty()) {
            dto.setActorDetails(List.of());
            return;
        }
        var normalized = actorNames.stream()
                .filter(n -> n != null && !n.isBlank())
                .map(String::trim)
                .toList();
        var details = normalized.stream()
                .map(name -> actorRepository.findByName(name).map(actor -> ActorCRUD.Response.builder()
                        .id(actor.getId())
                        .name(actor.getName())
                        .imageUrl(actor.getImageUrl())
                        .dob(actor.getDob())
                        .description(actor.getDescription())
                        .movieCount(actor.getMovies() != null ? actor.getMovies().size() : 0)
                        .build()).orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();
        dto.setActorDetails(details);
    }

    private void validateActorsExist(List<String> actorNames) {
        if (actorNames == null || actorNames.isEmpty()) {
            return;
        }
        var namesLower = actorNames.stream()
                .filter(n -> n != null && !n.isBlank())
                .map(String::trim)
                .map(String::toLowerCase)
                .toList();
        if (namesLower.isEmpty()) {
            return;
        }
        var existing = new java.util.HashSet<>(actorRepository.findExistingLowerNames(namesLower));
        var missing = namesLower.stream()
                .filter(n -> !existing.contains(n))
                .distinct()
                .toList();
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Unknown actors: " + String.join(", ", missing));
        }
    }
    
    private void validateDirectorExistsByName(String directorName) {
        if (directorName == null || directorName.trim().isEmpty()) {
            throw new IllegalArgumentException("Director name is required");
        }
        if (!directorRepository.existsByNameIgnoreCase(directorName.trim())) {
            throw new IllegalArgumentException("Director not found with name: " + directorName);
        }
    }

    private void validateCategoriesExist(List<String> namesLower) {
        if (namesLower == null || namesLower.isEmpty()) {
            return;
        }
        var existing = new java.util.HashSet<>(categoryRepository.findExistingLowerNames(namesLower));
        var missing = namesLower.stream()
                .filter(n -> !existing.contains(n))
                .distinct()
                .toList();
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Unknown categories: " + String.join(", ", missing));
        }
    }

    public PageResponse<MovieDTOs.MovieResponseDTO> getFeaturedMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("releaseDate").descending().and(Sort.by("createdAt").descending()));
        Page<Movie> moviePage = movieRepository.findByIsFeaturedTrueAndIsAvailableTrueAndReleased(java.time.LocalDate.now(), pageable);
        
        List<MovieDTOs.MovieResponseDTO> movies = moviePage.getContent().stream()
                .map(MovieMapper::toResponse)
                .toList();
        
        return PageResponse.<MovieDTOs.MovieResponseDTO>builder()
                .items(movies)
                .totalElements(moviePage.getTotalElements())
                .totalPages(moviePage.getTotalPages())
                .page(page)
                .size(size)
                .build();
    }

    public PageResponse<MovieDTOs.MovieResponseDTO> getTrendingMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("viewCount").descending());
        Page<Movie> moviePage = movieRepository.findByIsTrendingTrueAndIsAvailableTrue(pageable);
        
        List<MovieDTOs.MovieResponseDTO> movies = moviePage.getContent().stream()
                .map(MovieMapper::toResponse)
                .toList();
        
        return PageResponse.<MovieDTOs.MovieResponseDTO>builder()
                .items(movies)
                .totalElements(moviePage.getTotalElements())
                .totalPages(moviePage.getTotalPages())
                .page(page)
                .size(size)
                .build();
    }
}
