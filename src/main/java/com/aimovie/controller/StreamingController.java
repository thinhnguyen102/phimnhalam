package com.aimovie.controller;

import com.aimovie.dto.*;
import com.aimovie.dto.VideoResolutionDTOs.ResolutionChangeRequest;
import com.aimovie.dto.VideoResolutionDTOs.ResolutionChangeResponse;
import com.aimovie.entity.Movie;
import com.aimovie.entity.VideoResolution;
import com.aimovie.repository.MovieRepository;
import com.aimovie.repository.VideoResolutionRepository;
import com.aimovie.service.FFmpegService;
import com.aimovie.service.UserFeatureService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/streaming")
@RequiredArgsConstructor
@Slf4j
public class StreamingController {

    private final UserFeatureService userFeatureService;
    private final VideoResolutionRepository videoResolutionRepository;
    private final MovieRepository movieRepository;
    private final FFmpegService ffmpegService;

    // ==================== MOVIE STREAMING ====================

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<MovieStreamingDTO> getMovieForStreaming(
            @PathVariable Long movieId,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            MovieStreamingDTO movie = userFeatureService.getMovieForStreaming(userId, movieId);
            return ResponseEntity.ok(movie);
        } catch (RuntimeException e) {
            log.error("Movie not found with id: {}", movieId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting movie for streaming", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/start")
    public ResponseEntity<StreamingResponse> startStreaming(
            @Valid @RequestBody StreamingRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            StreamingResponse response = userFeatureService.startStreaming(userId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error starting streaming: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error starting streaming", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/progress/{movieId}")
    public ResponseEntity<Void> updateStreamingProgress(
            @PathVariable Long movieId,
            @RequestParam Integer currentTime,
            @RequestParam Integer totalTime,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            userFeatureService.updateStreamingProgress(userId, movieId, currentTime, totalTime);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error updating streaming progress", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== SUBTITLE MANAGEMENT ====================

    @GetMapping("/movie/{movieId}/subtitles")
    public ResponseEntity<List<SubtitleDTO>> getMovieSubtitles(@PathVariable Long movieId) {
        try {
            List<SubtitleDTO> subtitles = userFeatureService.getMovieSubtitles(movieId);
            return ResponseEntity.ok(subtitles);
        } catch (RuntimeException e) {
            log.error("Movie not found with id: {}", movieId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting movie subtitles", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/movie/{movieId}/subtitle/default")
    public ResponseEntity<SubtitleDTO> getDefaultSubtitle(@PathVariable Long movieId) {
        try {
            SubtitleDTO subtitle = userFeatureService.getDefaultSubtitle(movieId);
            if (subtitle != null) {
                return ResponseEntity.ok(subtitle);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            log.error("Movie not found with id: {}", movieId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting default subtitle", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/movie/{movieId}/subtitle/{languageCode}")
    public ResponseEntity<SubtitleDTO> getSubtitleByLanguage(
            @PathVariable Long movieId,
            @PathVariable String languageCode) {
        try {
            SubtitleDTO subtitle = userFeatureService.getSubtitleByLanguage(movieId, languageCode);
            if (subtitle != null) {
                return ResponseEntity.ok(subtitle);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            log.error("Movie not found with id: {}", movieId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting subtitle by language", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== QUALITY MANAGEMENT ====================

    @GetMapping("/movie/{movieId}/qualities")
    public ResponseEntity<ApiResponse<List<String>>> getAvailableQualities(@PathVariable Long movieId) {
        try {
            List<String> qualities = userFeatureService.getAvailableQualitiesByMovieId(movieId);
            ApiResponse<List<String>> apiResponse = new ApiResponse<>("SUCCESS", "Available qualities retrieved successfully", qualities);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error getting available qualities: {}", e.getMessage());
            ApiResponse<List<String>> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.badRequest().body(apiResponse);
        } catch (Exception e) {
            log.error("Error getting available qualities", e);
            ApiResponse<List<String>> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/movie/{movieId}/quality/{quality}")
    public ResponseEntity<ApiResponse<StreamingResponse>> getStreamingUrlByQuality(
            @PathVariable Long movieId,
            @PathVariable String quality,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            
            StreamingRequest streamingRequest = StreamingRequest.builder()
                    .movieId(movieId)
                    .quality(quality)
                    .build();
            
            StreamingResponse response = userFeatureService.startStreaming(userId, streamingRequest);
            ApiResponse<StreamingResponse> apiResponse = new ApiResponse<>("SUCCESS", "Streaming URL retrieved successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error getting streaming URL by quality: {}", e.getMessage());
            ApiResponse<StreamingResponse> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.badRequest().body(apiResponse);
        } catch (Exception e) {
            log.error("Error getting streaming URL by quality", e);
            ApiResponse<StreamingResponse> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    // ==================== RESOLUTION MANAGEMENT ====================

    @PostMapping("/movie/{movieId}/create-resolutions")
    public ResponseEntity<ApiResponse<Object>> createVideoResolutions(@PathVariable Long movieId) {
        try {
            log.info("Creating video resolutions for movie ID: {}", movieId);
            
            // Get movie
            Movie movie = movieRepository.findById(movieId)
                    .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));
            
            log.info("Found movie: {}", movie.getTitle());
            
            List<Map<String, Object>> createdResolutions = new ArrayList<>();
            
            // Create VideoResolution entities for movie ID 19
            if (movieId == 19) {
                log.info("Creating resolutions for movie 19");
                
                // 720p
                VideoResolution resolution720 = VideoResolution.builder()
                        .movie(movie)
                        .quality("720p")
                        .width(1280)
                        .height(720)
                        .videoUrl("/api/videos/stream/19/19_720p.mp4")
                        .videoFormat("mp4")
                        .fileSizeBytes(26016693L)
                        .bitrate(2500)
                        .isAvailable(true)
                        .encodingStatus("COMPLETED")
                        .encodingProgress(100)
                        .build();
                
                VideoResolution saved720 = videoResolutionRepository.save(resolution720);
                createdResolutions.add(Map.of(
                        "quality", "720p",
                        "videoUrl", saved720.getVideoUrl()
                ));
                log.info("Created 720p resolution: {}", saved720.getVideoUrl());
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("movieId", movieId);
            result.put("createdResolutions", createdResolutions);
            result.put("totalCreated", createdResolutions.size());
            
            ApiResponse<Object> apiResponse = new ApiResponse<>("SUCCESS", "Video resolutions created", result);
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            log.error("Error creating video resolutions for movie {}", movieId, e);
            ApiResponse<Object> apiResponse = new ApiResponse<>("ERROR", "Internal server error: " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/movie/{movieId}/resolutions")
    public ResponseEntity<ApiResponse<Object>> getMovieResolutions(@PathVariable Long movieId) {
        try {
            List<VideoResolution> resolutions = videoResolutionRepository.findByMovieId(movieId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("movieId", movieId);
            result.put("totalResolutions", resolutions.size());
            result.put("resolutions", resolutions.stream()
                    .map(vr -> Map.of(
                            "id", vr.getId(),
                            "quality", vr.getQuality(),
                            "width", vr.getWidth(),
                            "height", vr.getHeight(),
                            "videoUrl", vr.getVideoUrl(),
                            "isAvailable", vr.getIsAvailable(),
                            "encodingStatus", vr.getEncodingStatus(),
                            "encodingProgress", vr.getEncodingProgress()
                    ))
                    .collect(Collectors.toList()));
            
            ApiResponse<Object> apiResponse = new ApiResponse<>("SUCCESS", "Movie resolutions retrieved", result);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error getting movie resolutions", e);
            ApiResponse<Object> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @PostMapping("/movie/{movieId}/change-resolution")
    public ResponseEntity<ApiResponse<Object>> changeVideoResolution(
            @PathVariable Long movieId,
            @RequestParam String quality,
            @RequestParam(required = false) Integer currentTime,
            HttpServletRequest request) {
        try {
            ResolutionChangeRequest changeRequest = ResolutionChangeRequest.builder()
                    .movieId(movieId)
                    .quality(quality)
                    .currentTime(currentTime)
                    .build();
            
            ResolutionChangeResponse response = userFeatureService.changeVideoResolution(changeRequest);
            ApiResponse<Object> apiResponse = new ApiResponse<>("SUCCESS", "Resolution changed successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error changing video resolution: {}", e.getMessage());
            ApiResponse<Object> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.badRequest().body(apiResponse);
        } catch (Exception e) {
            log.error("Error changing video resolution", e);
            ApiResponse<Object> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/movie/{movieId}/available-resolutions")
    public ResponseEntity<ApiResponse<List<Object>>> getAvailableResolutions(@PathVariable Long movieId) {
        try {
            List<Object> resolutions = userFeatureService.getAvailableResolutionsByMovieId(movieId);
            ApiResponse<List<Object>> apiResponse = new ApiResponse<>("SUCCESS", "Available resolutions retrieved successfully", resolutions);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error getting available resolutions: {}", e.getMessage());
            ApiResponse<List<Object>> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.badRequest().body(apiResponse);
        } catch (Exception e) {
            log.error("Error getting available resolutions", e);
            ApiResponse<List<Object>> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/movie/{movieId}/best-quality")
    public ResponseEntity<ApiResponse<String>> getBestAvailableQuality(
            @PathVariable Long movieId,
            @RequestParam(required = false) String preferredQuality) {
        try {
            String bestQuality = userFeatureService.getBestAvailableQuality(movieId, preferredQuality);
            ApiResponse<String> apiResponse = new ApiResponse<>("SUCCESS", "Best available quality retrieved", bestQuality);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error getting best available quality: {}", e.getMessage());
            ApiResponse<String> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.badRequest().body(apiResponse);
        } catch (Exception e) {
            log.error("Error getting best available quality", e);
            ApiResponse<String> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/movie/{movieId}/check-resolution/{quality}")
    public ResponseEntity<ApiResponse<Boolean>> isResolutionAvailable(
            @PathVariable Long movieId,
            @PathVariable String quality) {
        try {
            Boolean isAvailable = userFeatureService.isResolutionAvailable(movieId, quality);
            ApiResponse<Boolean> apiResponse = new ApiResponse<>("SUCCESS", "Resolution availability checked", isAvailable);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error checking resolution availability", e);
            ApiResponse<Boolean> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    // ==================== STREAMING CONTROL ====================

    @PostMapping("/pause/{movieId}")
    public ResponseEntity<Void> pauseStreaming(
            @PathVariable Long movieId,
            @RequestParam Integer currentTime,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            // Update watch history with current progress
            userFeatureService.updateStreamingProgress(userId, movieId, currentTime, null);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error pausing streaming", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/resume/{movieId}")
    public ResponseEntity<StreamingResponse> resumeStreaming(
            @PathVariable Long movieId,
            @RequestParam(required = false) String quality,
            @RequestParam(required = false) String subtitleLanguage,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            
            StreamingRequest streamingRequest = StreamingRequest.builder()
                    .movieId(movieId)
                    .quality(quality)
                    .subtitleLanguage(subtitleLanguage)
                    .build();
            
            StreamingResponse response = userFeatureService.startStreaming(userId, streamingRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error resuming streaming", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/seek/{movieId}")
    public ResponseEntity<Void> seekToTime(
            @PathVariable Long movieId,
            @RequestParam Integer seekTime,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            // Update watch history with seek time
            userFeatureService.updateStreamingProgress(userId, movieId, seekTime, null);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error seeking to time", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== STREAMING ANALYTICS ====================

    @GetMapping("/analytics/{movieId}")
    public ResponseEntity<Object> getStreamingAnalytics(
            @PathVariable Long movieId,
            HttpServletRequest request) {
        try {
            // Implementation for streaming analytics
            // This could include watch time, completion rate, etc.
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error getting streaming analytics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== DEVICE MANAGEMENT ====================

    @PostMapping("/device/register")
    public ResponseEntity<Void> registerDevice(
            @RequestParam String deviceId,
            @RequestParam String deviceType,
            @RequestParam String deviceName,
            HttpServletRequest request) {
        try {
            // Implementation for device registration
            // This could be used for limiting concurrent streams
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error registering device", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/devices")
    public ResponseEntity<List<Object>> getUserDevices(HttpServletRequest request) {
        try {
            // Implementation for getting user's registered devices
            return ResponseEntity.ok(List.of());
        } catch (Exception e) {
            log.error("Error getting user devices", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/device/{deviceId}")
    public ResponseEntity<Void> removeDevice(
            @PathVariable String deviceId,
            HttpServletRequest request) {
        try {
            // Implementation for removing device
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error removing device", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== STREAMING HEALTH ====================

    @GetMapping("/health")
    public ResponseEntity<Object> getStreamingHealth() {
        try {
            // Implementation for streaming service health check
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error checking streaming health", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/status/{movieId}")
    public ResponseEntity<Object> getStreamingStatus(
            @PathVariable Long movieId,
            HttpServletRequest request) {
        try {
            // Implementation for getting streaming status
            // This could include current quality, buffering status, etc.
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error getting streaming status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
