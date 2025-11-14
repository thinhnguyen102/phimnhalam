package com.aimovie.controller;

import com.aimovie.dto.ApiResponse;
import com.aimovie.dto.MovieDTOs;
import com.aimovie.dto.PageResponse;
import com.aimovie.dto.PublicRatingDTO;
import com.aimovie.entity.Movie;
import com.aimovie.entity.Rating;
import com.aimovie.entity.VideoResolution;
import com.aimovie.repository.MovieRepository;
import com.aimovie.repository.RatingRepository;
import com.aimovie.repository.VideoResolutionRepository;
import com.aimovie.service.FileUploadService;
import com.aimovie.service.FFmpegService;
import com.aimovie.service.MovieService;
import com.aimovie.service.VideoMetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@Slf4j
public class MovieController {

    private final MovieService movieService;
    private final FileUploadService fileUploadService;
    private final FFmpegService ffmpegService;
    private final MovieRepository movieRepository;
    private final VideoResolutionRepository videoResolutionRepository;
    private final RatingRepository ratingRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieDTOs.MovieResponseDTO> createMovie(
            @RequestParam("title") String title,
            @RequestParam(value = "synopsis", required = false) String synopsis,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "posterUrl", required = false) String posterUrl,
            @RequestParam(value = "thumbnailUrl", required = false) String thumbnailUrl,
            @RequestParam(value = "videoUrl", required = false) String videoUrl,
            @RequestParam(value = "videoFormat", required = false) String videoFormat,
            @RequestParam(value = "videoDuration", required = false) Integer videoDuration,
            @RequestParam(value = "videoQuality", required = false) String videoQuality,
            @RequestParam(value = "fileSizeBytes", required = false) Long fileSizeBytes,
            @RequestParam(value = "streamingUrl", required = false) String streamingUrl,
            @RequestParam(value = "isAvailable", required = false) Boolean isAvailable,
            @RequestParam(value = "actors", required = false) String actors,
            @RequestParam(value = "directors", required = false) String directors,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "language", required = false) String language,
            @RequestParam(value = "ageRating", required = false) String ageRating,
            @RequestParam(value = "imdbRating", required = false) Double imdbRating,
            @RequestParam(value = "viewCount", required = false) Long viewCount,
            @RequestParam(value = "likeCount", required = false) Long likeCount,
            @RequestParam(value = "dislikeCount", required = false) Long dislikeCount,
            @RequestParam(value = "averageRating", required = false) Double averageRating,
            @RequestParam(value = "totalRatings", required = false) Long totalRatings,
            @RequestParam(value = "isFeatured", required = false) Boolean isFeatured,
            @RequestParam(value = "isTrending", required = false) Boolean isTrending,
            @RequestParam(value = "releaseDate", required = false) String releaseDate,
            @RequestParam(value = "trailerUrl", required = false) String trailerUrl,
            @RequestParam(value = "downloadEnabled", required = false) Boolean downloadEnabled,
            @RequestParam(value = "maxDownloadQuality", required = false) String maxDownloadQuality,
            @RequestParam(value = "availableQualities", required = false) String availableQualities) {
        
        MovieDTOs.MovieCreateDTO createDTO = new MovieDTOs.MovieCreateDTO();
        createDTO.setTitle(title);
        createDTO.setSynopsis(synopsis);
        createDTO.setYear(year);
        createDTO.setPosterUrl(posterUrl);
        createDTO.setThumbnailUrl(thumbnailUrl);
        createDTO.setVideoUrl(videoUrl);
        createDTO.setVideoFormat(videoFormat);
        createDTO.setVideoDuration(videoDuration);
        createDTO.setVideoQuality(videoQuality);
        createDTO.setFileSizeBytes(fileSizeBytes);
        createDTO.setStreamingUrl(streamingUrl);
        createDTO.setIsAvailable(isAvailable);
        
        if (actors != null && !actors.isEmpty()) {
            createDTO.setActors(java.util.Arrays.asList(actors.split(",")));
        }
        if (availableQualities != null && !availableQualities.isEmpty()) {
            createDTO.setAvailableQualities(java.util.Arrays.asList(availableQualities.split(",")));
        }
        
        createDTO.setCountry(country);
        createDTO.setLanguage(language);
        createDTO.setAgeRating(ageRating);
        createDTO.setImdbRating(imdbRating);
        createDTO.setViewCount(viewCount);
        createDTO.setLikeCount(likeCount);
        createDTO.setDislikeCount(dislikeCount);
        createDTO.setAverageRating(averageRating);
        createDTO.setTotalRatings(totalRatings);
        createDTO.setIsFeatured(isFeatured);
        createDTO.setIsTrending(isTrending);
        if (releaseDate != null && !releaseDate.isEmpty()) {
            createDTO.setReleaseDate(java.time.LocalDate.parse(releaseDate));
        }
        createDTO.setTrailerUrl(trailerUrl);
        createDTO.setDownloadEnabled(downloadEnabled);
        createDTO.setMaxDownloadQuality(maxDownloadQuality);
        
        MovieDTOs.MovieResponseDTO response = movieService.createMovie(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieDTOs.MovieResponseDTO> getMovieById(@PathVariable Long id) {
        MovieDTOs.MovieResponseDTO response = movieService.getMovieById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<MovieDTOs.MovieResponseDTO>> getAllMovies() {
        List<MovieDTOs.MovieResponseDTO> response = movieService.getAllMovies();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<MovieDTOs.MovieResponseDTO>> searchMovies(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<MovieDTOs.MovieResponseDTO> response = movieService.searchMovies(q, page, size);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieDTOs.MovieResponseDTO> updateMovie(
            @PathVariable Long id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "synopsis", required = false) String synopsis,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "posterUrl", required = false) String posterUrl,
            @RequestParam(value = "thumbnailUrl", required = false) String thumbnailUrl,
            @RequestParam(value = "videoUrl", required = false) String videoUrl,
            @RequestParam(value = "videoFormat", required = false) String videoFormat,
            @RequestParam(value = "videoDuration", required = false) Integer videoDuration,
            @RequestParam(value = "videoQuality", required = false) String videoQuality,
            @RequestParam(value = "fileSizeBytes", required = false) Long fileSizeBytes,
            @RequestParam(value = "streamingUrl", required = false) String streamingUrl,
            @RequestParam(value = "isAvailable", required = false) Boolean isAvailable,
            @RequestParam(value = "actors", required = false) String actors,
            @RequestParam(value = "directors", required = false) String directors,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "language", required = false) String language,
            @RequestParam(value = "ageRating", required = false) String ageRating,
            @RequestParam(value = "imdbRating", required = false) Double imdbRating,
            @RequestParam(value = "viewCount", required = false) Long viewCount,
            @RequestParam(value = "likeCount", required = false) Long likeCount,
            @RequestParam(value = "dislikeCount", required = false) Long dislikeCount,
            @RequestParam(value = "averageRating", required = false) Double averageRating,
            @RequestParam(value = "totalRatings", required = false) Long totalRatings,
            @RequestParam(value = "isFeatured", required = false) Boolean isFeatured,
            @RequestParam(value = "isTrending", required = false) Boolean isTrending,
            @RequestParam(value = "releaseDate", required = false) String releaseDate,
            @RequestParam(value = "trailerUrl", required = false) String trailerUrl,
            @RequestParam(value = "downloadEnabled", required = false) Boolean downloadEnabled,
            @RequestParam(value = "maxDownloadQuality", required = false) String maxDownloadQuality,
            @RequestParam(value = "availableQualities", required = false) String availableQualities) {
        
        MovieDTOs.MovieUpdateDTO updateDTO = new MovieDTOs.MovieUpdateDTO();
        updateDTO.setTitle(title);
        updateDTO.setSynopsis(synopsis);
        updateDTO.setYear(year);
        updateDTO.setPosterUrl(posterUrl);
        updateDTO.setThumbnailUrl(thumbnailUrl);
        updateDTO.setVideoUrl(videoUrl);
        updateDTO.setVideoFormat(videoFormat);
        updateDTO.setVideoDuration(videoDuration);
        updateDTO.setVideoQuality(videoQuality);
        updateDTO.setFileSizeBytes(fileSizeBytes);
        updateDTO.setStreamingUrl(streamingUrl);
        updateDTO.setIsAvailable(isAvailable);
        
        if (actors != null && !actors.isEmpty()) {
            updateDTO.setActors(java.util.Arrays.asList(actors.split(",")));
        }
        if (availableQualities != null && !availableQualities.isEmpty()) {
            updateDTO.setAvailableQualities(java.util.Arrays.asList(availableQualities.split(",")));
        }
        
        updateDTO.setCountry(country);
        updateDTO.setLanguage(language);
        updateDTO.setAgeRating(ageRating);
        updateDTO.setImdbRating(imdbRating);
        updateDTO.setViewCount(viewCount);
        updateDTO.setLikeCount(likeCount);
        updateDTO.setDislikeCount(dislikeCount);
        updateDTO.setAverageRating(averageRating);
        updateDTO.setTotalRatings(totalRatings);
        updateDTO.setIsFeatured(isFeatured);
        updateDTO.setIsTrending(isTrending);
        if (releaseDate != null && !releaseDate.isEmpty()) {
            updateDTO.setReleaseDate(java.time.LocalDate.parse(releaseDate));
        }
        updateDTO.setTrailerUrl(trailerUrl);
        updateDTO.setDownloadEnabled(downloadEnabled);
        updateDTO.setMaxDownloadQuality(maxDownloadQuality);
        
        MovieDTOs.MovieResponseDTO response = movieService.updateMovie(id, updateDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FileUploadResponse> uploadVideo(@RequestParam("file") MultipartFile file) {
        try {
            FileUploadService.VideoUploadResult uploadResult = fileUploadService.uploadVideoWithMetadata(file);
            
            FileUploadResponse response = new FileUploadResponse(
                uploadResult.getFilename(), 
                uploadResult.getOriginalFilename(), 
                uploadResult.getMetadata().getFileSizeBytes(),
                "/api/videos/stream/" + uploadResult.getFilename(),
                uploadResult.getMetadata()
            );
            
            log.info("Video uploaded successfully with metadata: {}", uploadResult.getMetadata());
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Error uploading video file", e);
            return ResponseEntity.badRequest().build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid file: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/upload-video")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VideoUploadResponse> uploadMovieVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("movieId") Long movieId) {
        try {
            // Delete old main video file and resolution directory, if any
            try {
                MovieDTOs.MovieResponseDTO current = movieService.getMovieById(movieId);
                if (current.getVideoUrl() != null && !current.getVideoUrl().isEmpty()) {
                    String oldFilename = current.getVideoUrl().substring(current.getVideoUrl().lastIndexOf("/") + 1);
                    fileUploadService.deleteVideoFile(oldFilename);
                }
                java.nio.file.Path movieVideoDir = java.nio.file.Paths.get("uploads", "videos", String.valueOf(movieId));
                if (java.nio.file.Files.exists(movieVideoDir)) {
                    java.nio.file.Files.walk(movieVideoDir)
                            .sorted(java.util.Comparator.reverseOrder())
                            .forEach(path -> {
                                try {
                                    java.nio.file.Files.delete(path);
                                } catch (java.io.IOException ignored) {}
                            });
                }
            } catch (Exception ignored) {}

            FileUploadService.VideoUploadResult uploadResult = fileUploadService.uploadVideoWithMetadata(file);
            VideoMetadataService.VideoMetadata metadata = uploadResult.getMetadata();
            
            // Update movie with video URL
            MovieDTOs.MovieUpdateDTO updateDTO = new MovieDTOs.MovieUpdateDTO();
            updateDTO.setVideoUrl(fileUploadService.buildPublicVideoUrl(uploadResult.getFilename()));
            updateDTO.setVideoFormat(metadata.getVideoFormat());
            updateDTO.setVideoDuration(metadata.getDurationInSeconds());
            updateDTO.setVideoQuality("1440p");
            updateDTO.setFileSizeBytes(metadata.getFileSizeBytes());
            updateDTO.setAvailableQualities(Arrays.asList("360p", "480p", "720p", "1080p", "1440p"));
            updateDTO.setMaxDownloadQuality("1440p");
            
            movieService.updateMovie(movieId, updateDTO);
            
            // Start async FFmpeg processing for multiple resolutions
            try {
                Path inputVideoPath = Paths.get("uploads/videos", uploadResult.getFilename());
                if (Files.exists(inputVideoPath)) {
                    ffmpegService.processVideoToMultipleResolutions(inputVideoPath, movieId, uploadResult.getFilename())
                            .thenAccept(result -> {
                                if (result.isSuccess()) {
                                    log.info("Video processing completed successfully for movie ID: {}", movieId);
                                } else {
                                    log.error("Video processing failed for movie ID: {} - {}", movieId, result.getErrorMessage());
                                }
                            })
                            .exceptionally(throwable -> {
                                log.error("Error during video processing for movie ID: {}", movieId, throwable);
                                return null;
                            });
                    log.info("Started video processing for multiple resolutions for movie ID: {}", movieId);
                } else {
                    log.warn("Video file not found for processing: {}", inputVideoPath);
                }
            } catch (Exception e) {
                log.error("Failed to start video processing for movie ID: {}", movieId, e);
            }
            
            VideoUploadResponse response = new VideoUploadResponse(
                "Video uploaded successfully",
                "/api/videos/stream/" + uploadResult.getFilename(),
                metadata.getFileSizeBytes(),
                metadata.getDurationInSeconds(),
                metadata.getVideoQuality()
            );
            
            log.info("Video uploaded successfully for movie {}: {}", movieId, uploadResult.getFilename());
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Error uploading video file for movie {}", movieId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid file for movie {}: {}", movieId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating movie {} with video", movieId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/create-with-video")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieDTOs.MovieResponseDTO> createMovieWithVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("synopsis") String synopsis,
            @RequestParam("year") Integer year,
            @RequestParam(value = "posterUrl", required = false) String posterUrl) {
        
        try {
            FileUploadService.VideoUploadResult uploadResult = fileUploadService.uploadVideoWithMetadata(file);
            VideoMetadataService.VideoMetadata metadata = uploadResult.getMetadata();
            
            MovieDTOs.MovieCreateDTO createDTO = new MovieDTOs.MovieCreateDTO();
            createDTO.setTitle(title);
            createDTO.setSynopsis(synopsis);
            createDTO.setYear(year);
            createDTO.setPosterUrl(posterUrl);
            createDTO.setVideoUrl(fileUploadService.buildPublicVideoUrl(uploadResult.getFilename()));
            createDTO.setVideoFormat(metadata.getVideoFormat());
            createDTO.setVideoDuration(metadata.getDurationInSeconds()); 
            createDTO.setVideoQuality("1440p");
            createDTO.setFileSizeBytes(metadata.getFileSizeBytes());
            createDTO.setIsAvailable(true);
            createDTO.setAvailableQualities(List.of());
            createDTO.setMaxDownloadQuality(null);
            
            MovieDTOs.MovieResponseDTO response = movieService.createMovie(createDTO);
            
            // Start async FFmpeg processing for multiple resolutions
            try {
                Path inputVideoPath = Paths.get("uploads/videos", uploadResult.getFilename());
                if (Files.exists(inputVideoPath)) {
                    ffmpegService.processVideoToMultipleResolutions(inputVideoPath, response.getId(), uploadResult.getFilename())
                            .thenAccept(result -> {
                                if (result.isSuccess()) {
                                    log.info("Video processing completed successfully for movie ID: {}", response.getId());
                                } else {
                                    log.error("Video processing failed for movie ID: {} - {}", response.getId(), result.getErrorMessage());
                                }
                            })
                            .exceptionally(throwable -> {
                                log.error("Error during video processing for movie ID: {}", response.getId(), throwable);
                                return null;
                            });
                    log.info("Started video processing for multiple resolutions for movie ID: {}", response.getId());
                } else {
                    log.warn("Video file not found for processing: {}", inputVideoPath);
                }
            } catch (Exception e) {
                log.error("Failed to start video processing for movie ID: {}", response.getId(), e);
            }
            
            log.info("Movie created with video: {} (file: {}, metadata: {})", title, uploadResult.getFilename(), metadata);
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Error creating movie with video", e);
            return ResponseEntity.badRequest().build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid file: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/create-form")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieDTOs.MovieResponseDTO> createMovieWithForm(
            @RequestParam(value = "poster", required = false) MultipartFile poster,
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestParam(value = "video", required = false) MultipartFile video,
            @RequestParam("title") String title,
            @RequestParam(value = "synopsis", required = false) String synopsis,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "categories", required = false) String categoriesCsv,
            @RequestParam(value = "actors", required = false) String actorsCsv,
            @RequestParam("directorName") String directorName,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "language", required = false) String language,
            @RequestParam(value = "ageRating", required = false) String ageRating,
            @RequestParam(value = "imdbRating", required = false) Double imdbRating,
            @RequestParam(value = "isAvailable", required = false) Boolean isAvailable,
            @RequestParam(value = "isFeatured", required = false) Boolean isFeatured,
            @RequestParam(value = "isTrending", required = false) Boolean isTrending,
            @RequestParam(value = "releaseDate", required = false) java.time.LocalDate releaseDate,
            @RequestParam(value = "trailerUrl", required = false) String trailerUrl
    ) {
        try {
            MovieDTOs.MovieCreateDTO dto = new MovieDTOs.MovieCreateDTO();
            dto.setTitle(title);
            dto.setSynopsis(synopsis);
            dto.setYear(year);
            dto.setCountry(country);
            dto.setLanguage(language);
            dto.setAgeRating(ageRating);
            dto.setImdbRating(imdbRating);
            dto.setIsAvailable(isAvailable);
            dto.setIsFeatured(isFeatured);
            dto.setIsTrending(isTrending);
            dto.setReleaseDate(releaseDate);
            dto.setTrailerUrl(trailerUrl);

            if (categoriesCsv != null && !categoriesCsv.isBlank()) {
                dto.setCategories(Arrays.stream(categoriesCsv.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(String::toLowerCase)
                        .collect(Collectors.toList()));
            }
            if (actorsCsv != null && !actorsCsv.isBlank()) {
                dto.setActors(Arrays.stream(actorsCsv.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList()));
            }
            dto.setDirectorName(directorName);

            if (poster != null && !poster.isEmpty()) {
                String posterFilename = fileUploadService.uploadImageFile(poster);
                dto.setPosterUrl(fileUploadService.buildPublicImageUrl(posterFilename));
            }
            if (thumbnail != null && !thumbnail.isEmpty()) {
                String thumbFilename = fileUploadService.uploadImageFile(thumbnail);
                dto.setThumbnailUrl(fileUploadService.buildPublicImageUrl(thumbFilename));
            }
            MovieDTOs.MovieResponseDTO response = movieService.createMovie(dto);
            
            // Process video to multiple resolutions after movie creation
            if (video != null && !video.isEmpty()) {
                FileUploadService.VideoUploadResult uploadResult = fileUploadService.uploadVideoWithMetadata(video);
                var metadata = uploadResult.getMetadata();
                
                // Update movie with video info
                MovieDTOs.MovieUpdateDTO updateDTO = new MovieDTOs.MovieUpdateDTO();
                updateDTO.setVideoUrl(fileUploadService.buildPublicVideoUrl(uploadResult.getFilename()));
                updateDTO.setVideoFormat(metadata.getVideoFormat());
                updateDTO.setVideoDuration(metadata.getDurationInSeconds());
                updateDTO.setVideoQuality(metadata.getVideoQuality());
                updateDTO.setFileSizeBytes(metadata.getFileSizeBytes());
                updateDTO.setAvailableQualities(List.of("360p", "720p", "1080p"));
                updateDTO.setMaxDownloadQuality("1080p");
                
                movieService.updateMovie(response.getId(), updateDTO);
                
                // Start async FFmpeg processing for multiple resolutions
                try {
                    Path inputVideoPath = Paths.get("uploads/videos", uploadResult.getFilename());
                    if (Files.exists(inputVideoPath)) {
                        ffmpegService.processVideoToMultipleResolutions(inputVideoPath, response.getId(), uploadResult.getFilename())
                                .thenAccept(result -> {
                                    if (result.isSuccess()) {
                                        log.info("Video processing completed successfully for movie ID: {}", response.getId());
                                    } else {
                                        log.error("Video processing failed for movie ID: {} - {}", response.getId(), result.getErrorMessage());
                                    }
                                })
                                .exceptionally(throwable -> {
                                    log.error("Error during video processing for movie ID: {}", response.getId(), throwable);
                                    return null;
                                });
                        log.info("Started video processing for multiple resolutions for movie ID: {}", response.getId());
                    } else {
                        log.warn("Video file not found for processing: {}", inputVideoPath);
                    }
                } catch (Exception e) {
                    log.error("Failed to start video processing for movie ID: {}", response.getId(), e);
                }
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating movie via form", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/video-processing-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getVideoProcessingStatus(@PathVariable Long id) {
        try {
            // Check if movie exists
            Movie movie = movieRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Movie not found"));
            
            // Get video resolutions for this movie
            List<VideoResolution> resolutions = videoResolutionRepository.findByMovieId(id);
            
            Map<String, Object> status = new HashMap<>();
            status.put("movieId", id);
            status.put("movieTitle", movie.getTitle());
            status.put("totalResolutions", resolutions.size());
            status.put("availableResolutions", resolutions.stream()
                    .filter(VideoResolution::getIsAvailable)
                    .map(VideoResolution::getQuality)
                    .collect(Collectors.toList()));
            status.put("processingResolutions", resolutions.stream()
                    .filter(r -> "PROCESSING".equals(r.getEncodingStatus()))
                    .map(VideoResolution::getQuality)
                    .collect(Collectors.toList()));
            status.put("completedResolutions", resolutions.stream()
                    .filter(r -> "COMPLETED".equals(r.getEncodingStatus()))
                    .map(VideoResolution::getQuality)
                    .collect(Collectors.toList()));
            status.put("failedResolutions", resolutions.stream()
                    .filter(r -> "FAILED".equals(r.getEncodingStatus()))
                    .map(VideoResolution::getQuality)
                    .collect(Collectors.toList()));
            
            ApiResponse<Object> apiResponse = new ApiResponse<>("SUCCESS", "Video processing status retrieved", status);
            return ResponseEntity.ok(apiResponse);
            
        } catch (RuntimeException e) {
            log.error("Error getting video processing status: {}", e.getMessage());
            ApiResponse<Object> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.badRequest().body(apiResponse);
        } catch (Exception e) {
            log.error("Error getting video processing status", e);
            ApiResponse<Object> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    public static class FileUploadResponse {
        private String filename;
        private String originalFilename;
        private long fileSize;
        private String streamUrl;
        private VideoMetadataService.VideoMetadata metadata;

        public FileUploadResponse(String filename, String originalFilename, long fileSize, String streamUrl, VideoMetadataService.VideoMetadata metadata) {
            this.filename = filename;
            this.originalFilename = originalFilename;
            this.fileSize = fileSize;
            this.streamUrl = streamUrl;
            this.metadata = metadata;
        }

        public String getFilename() { return filename; }
        public String getOriginalFilename() { return originalFilename; }
        public long getFileSize() { return fileSize; }
        public String getStreamUrl() { return streamUrl; }
        public VideoMetadataService.VideoMetadata getMetadata() { return metadata; }
    }

    public static class VideoUploadResponse {
        private String message;
        private String videoUrl;
        private long fileSize;
        private int duration;
        private String quality;

        public VideoUploadResponse(String message, String videoUrl, long fileSize, int duration, String quality) {
            this.message = message;
            this.videoUrl = videoUrl;
            this.fileSize = fileSize;
            this.duration = duration;
            this.quality = quality;
        }

        public String getMessage() { return message; }
        public String getVideoUrl() { return videoUrl; }
        public long getFileSize() { return fileSize; }
        public int getDuration() { return duration; }
        public String getQuality() { return quality; }
    }

    public static class PosterUploadResponse {
        private String message;
        private String posterUrl;
        private long fileSize;

        public PosterUploadResponse(String message, String posterUrl, long fileSize) {
            this.message = message;
            this.posterUrl = posterUrl;
            this.fileSize = fileSize;
        }

        public String getMessage() { return message; }
        public String getPosterUrl() { return posterUrl; }
        public long getFileSize() { return fileSize; }
    }


    @GetMapping("/genre/{genre}")
    public ResponseEntity<PageResponse<MovieDTOs.MovieResponseDTO>> getMoviesByGenre(
            @PathVariable String genre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<MovieDTOs.MovieResponseDTO> response = movieService.getMoviesByGenre(genre, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/year/{year}")
    public ResponseEntity<PageResponse<MovieDTOs.MovieResponseDTO>> getMoviesByYear(
            @PathVariable Integer year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<MovieDTOs.MovieResponseDTO> response = movieService.getMoviesByYear(year, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/year-range")
    public ResponseEntity<PageResponse<MovieDTOs.MovieResponseDTO>> getMoviesByYearRange(
            @RequestParam Integer startYear,
            @RequestParam Integer endYear,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<MovieDTOs.MovieResponseDTO> response = movieService.getMoviesByYearRange(startYear, endYear, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available")
    public ResponseEntity<PageResponse<MovieDTOs.MovieResponseDTO>> getAvailableMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<MovieDTOs.MovieResponseDTO> response = movieService.getAvailableMovies(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unavailable")
    public ResponseEntity<PageResponse<MovieDTOs.MovieResponseDTO>> getUnavailableMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<MovieDTOs.MovieResponseDTO> response = movieService.getUnavailableMovies(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/latest")
    public ResponseEntity<PageResponse<MovieDTOs.MovieResponseDTO>> getLatestMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<MovieDTOs.MovieResponseDTO> response = movieService.getLatestMovies(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/advanced-search")
    public ResponseEntity<PageResponse<MovieDTOs.MovieResponseDTO>> advancedSearch(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<MovieDTOs.MovieResponseDTO> response = movieService.advancedSearch(q, page, size);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/toggle-availability")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieDTOs.MovieResponseDTO> toggleAvailability(@PathVariable Long id) {
        MovieDTOs.MovieResponseDTO response = movieService.toggleAvailability(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/poster")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieDTOs.MovieResponseDTO> uploadPoster(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            MovieDTOs.MovieResponseDTO response = movieService.uploadPoster(id, file);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Failed to upload poster for movie {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/thumbnail")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieDTOs.MovieResponseDTO> uploadThumbnail(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            MovieDTOs.MovieResponseDTO response = movieService.uploadThumbnail(id, file);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Error uploading thumbnail for movie {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/upload-poster")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PosterUploadResponse> uploadMoviePoster(
            @RequestParam("file") MultipartFile file,
            @RequestParam("movieId") Long movieId) {
        try {
            MovieDTOs.MovieResponseDTO response = movieService.uploadPoster(movieId, file);
            
            PosterUploadResponse uploadResponse = new PosterUploadResponse(
                "Poster uploaded successfully",
                response.getPosterUrl(),
                file.getSize()
            );
            
            log.info("Poster uploaded successfully for movie {}: {}", movieId, response.getPosterUrl());
            return ResponseEntity.ok(uploadResponse);
            
        } catch (IOException e) {
            log.error("Failed to upload poster for movie {}: {}", movieId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Error uploading poster for movie {}", movieId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/genres")
    public ResponseEntity<List<String>> getAllGenres() {
        // Genres removed, return empty list
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/years")
    public ResponseEntity<List<Integer>> getAllYears() {
        List<Integer> years = movieService.getAllYears();
        return ResponseEntity.ok(years);
    }

    @GetMapping("/featured")
    public ResponseEntity<PageResponse<MovieDTOs.MovieResponseDTO>> getFeaturedMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<MovieDTOs.MovieResponseDTO> response = movieService.getFeaturedMovies(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/trending")
    public ResponseEntity<PageResponse<MovieDTOs.MovieResponseDTO>> getTrendingMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<MovieDTOs.MovieResponseDTO> response = movieService.getTrendingMovies(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recent-ratings")
    public ResponseEntity<List<PublicRatingDTO>> getRecentRatings(
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<Rating> ratings = ratingRepository.findTop5ByCommentIsNotNullOrderByCreatedAtDesc();
            
            List<PublicRatingDTO> ratingDTOs = ratings.stream()
                    .limit(limit)
                    .map(rating -> PublicRatingDTO.builder()
                            .id(rating.getId())
                            .stars(rating.getStars())
                            .comment(rating.getComment())
                            .username(rating.getUser().getUsername())
                            .userAvatarUrl(rating.getUser().getAvatarUrl())
                            .movieTitle(rating.getMovie().getTitle())
                            .movieId(rating.getMovie().getId())
                            .moviePosterUrl(rating.getMovie().getPosterUrl())
                            .createdAt(rating.getCreatedAt())
                            .build())
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ratingDTOs);
        } catch (Exception e) {
            log.error("Error getting recent ratings", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
