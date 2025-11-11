package com.aimovie.controller;

import com.aimovie.dto.ApiResponse;
import com.aimovie.dto.VideoResolutionDTOs.*;
import com.aimovie.service.VideoResolutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/video-resolutions")
@RequiredArgsConstructor
@Slf4j
public class VideoResolutionController {

    private final VideoResolutionService videoResolutionService;

    @PostMapping
    public ResponseEntity<ApiResponse<VideoResolutionResponse>> createVideoResolution(
            @Valid @RequestBody VideoResolutionRequest request) {
        try {
            VideoResolutionResponse response = videoResolutionService.createVideoResolution(request);
            ApiResponse<VideoResolutionResponse> apiResponse = new ApiResponse<>("SUCCESS", "Video resolution created successfully", response);
            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error creating video resolution: {}", e.getMessage());
            ApiResponse<VideoResolutionResponse> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.badRequest().body(apiResponse);
        } catch (Exception e) {
            log.error("Error creating video resolution", e);
            ApiResponse<VideoResolutionResponse> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VideoResolutionResponse>> updateVideoResolution(
            @PathVariable Long id,
            @Valid @RequestBody VideoResolutionUpdateRequest request) {
        try {
            VideoResolutionResponse response = videoResolutionService.updateVideoResolution(id, request);
            ApiResponse<VideoResolutionResponse> apiResponse = new ApiResponse<>("SUCCESS", "Video resolution updated successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error updating video resolution: {}", e.getMessage());
            ApiResponse<VideoResolutionResponse> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.badRequest().body(apiResponse);
        } catch (Exception e) {
            log.error("Error updating video resolution", e);
            ApiResponse<VideoResolutionResponse> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VideoResolutionResponse>> getVideoResolutionById(@PathVariable Long id) {
        try {
            VideoResolutionResponse response = videoResolutionService.getVideoResolutionById(id);
            ApiResponse<VideoResolutionResponse> apiResponse = new ApiResponse<>("SUCCESS", "Video resolution retrieved successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error getting video resolution: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting video resolution", e);
            ApiResponse<VideoResolutionResponse> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<ApiResponse<List<VideoResolutionResponse>>> getVideoResolutionsByMovieId(@PathVariable Long movieId) {
        try {
            List<VideoResolutionResponse> response = videoResolutionService.getVideoResolutionsByMovieId(movieId);
            ApiResponse<List<VideoResolutionResponse>> apiResponse = new ApiResponse<>("SUCCESS", "Video resolutions retrieved successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error getting video resolutions by movie id", e);
            ApiResponse<List<VideoResolutionResponse>> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/movie/{movieId}/available")
    public ResponseEntity<ApiResponse<List<AvailableResolutionResponse>>> getAvailableResolutionsByMovieId(@PathVariable Long movieId) {
        try {
            List<AvailableResolutionResponse> response = videoResolutionService.getAvailableResolutionsByMovieId(movieId);
            ApiResponse<List<AvailableResolutionResponse>> apiResponse = new ApiResponse<>("SUCCESS", "Available resolutions retrieved successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error getting available resolutions by movie id", e);
            ApiResponse<List<AvailableResolutionResponse>> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/movie/{movieId}/qualities")
    public ResponseEntity<ApiResponse<List<String>>> getAvailableQualitiesByMovieId(@PathVariable Long movieId) {
        try {
            List<String> response = videoResolutionService.getAvailableQualitiesByMovieId(movieId);
            ApiResponse<List<String>> apiResponse = new ApiResponse<>("SUCCESS", "Available qualities retrieved successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error getting available qualities by movie id", e);
            ApiResponse<List<String>> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/movie/{movieId}/quality/{quality}")
    public ResponseEntity<ApiResponse<VideoResolutionResponse>> getVideoResolutionByMovieIdAndQuality(
            @PathVariable Long movieId,
            @PathVariable String quality) {
        try {
            VideoResolutionResponse response = videoResolutionService.getVideoResolutionByMovieIdAndQuality(movieId, quality);
            ApiResponse<VideoResolutionResponse> apiResponse = new ApiResponse<>("SUCCESS", "Video resolution retrieved successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error getting video resolution by quality: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting video resolution by quality", e);
            ApiResponse<VideoResolutionResponse> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @PostMapping("/change-resolution")
    public ResponseEntity<ApiResponse<ResolutionChangeResponse>> changeVideoResolution(
            @Valid @RequestBody ResolutionChangeRequest request) {
        try {
            ResolutionChangeResponse response = videoResolutionService.changeVideoResolution(request);
            ApiResponse<ResolutionChangeResponse> apiResponse = new ApiResponse<>("SUCCESS", "Resolution change processed", response);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error changing video resolution: {}", e.getMessage());
            ApiResponse<ResolutionChangeResponse> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.badRequest().body(apiResponse);
        } catch (Exception e) {
            log.error("Error changing video resolution", e);
            ApiResponse<ResolutionChangeResponse> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVideoResolution(@PathVariable Long id) {
        try {
            videoResolutionService.deleteVideoResolution(id);
            ApiResponse<Void> apiResponse = new ApiResponse<>("SUCCESS", "Video resolution deleted successfully", null);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error deleting video resolution: {}", e.getMessage());
            ApiResponse<Void> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.badRequest().body(apiResponse);
        } catch (Exception e) {
            log.error("Error deleting video resolution", e);
            ApiResponse<Void> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @DeleteMapping("/movie/{movieId}")
    public ResponseEntity<ApiResponse<Void>> deleteVideoResolutionsByMovieId(@PathVariable Long movieId) {
        try {
            videoResolutionService.deleteVideoResolutionsByMovieId(movieId);
            ApiResponse<Void> apiResponse = new ApiResponse<>("SUCCESS", "Video resolutions deleted successfully", null);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error deleting video resolutions by movie id", e);
            ApiResponse<Void> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/encoding-status/{status}")
    public ResponseEntity<ApiResponse<List<VideoResolutionResponse>>> getVideoResolutionsByEncodingStatus(@PathVariable String status) {
        try {
            List<VideoResolutionResponse> response = videoResolutionService.getVideoResolutionsByEncodingStatus(status);
            ApiResponse<List<VideoResolutionResponse>> apiResponse = new ApiResponse<>("SUCCESS", "Video resolutions retrieved successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error getting video resolutions by encoding status", e);
            ApiResponse<List<VideoResolutionResponse>> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @PutMapping("/{id}/encoding-status")
    public ResponseEntity<ApiResponse<VideoResolutionResponse>> updateEncodingStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) Integer progress) {
        try {
            VideoResolutionResponse response = videoResolutionService.updateEncodingStatus(id, status, progress);
            ApiResponse<VideoResolutionResponse> apiResponse = new ApiResponse<>("SUCCESS", "Encoding status updated successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error updating encoding status: {}", e.getMessage());
            ApiResponse<VideoResolutionResponse> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.badRequest().body(apiResponse);
        } catch (Exception e) {
            log.error("Error updating encoding status", e);
            ApiResponse<VideoResolutionResponse> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/movie/{movieId}/check-availability/{quality}")
    public ResponseEntity<ApiResponse<Boolean>> isResolutionAvailable(
            @PathVariable Long movieId,
            @PathVariable String quality) {
        try {
            Boolean response = videoResolutionService.isResolutionAvailable(movieId, quality);
            ApiResponse<Boolean> apiResponse = new ApiResponse<>("SUCCESS", "Resolution availability checked", response);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error checking resolution availability", e);
            ApiResponse<Boolean> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/movie/{movieId}/best-quality")
    public ResponseEntity<ApiResponse<String>> getBestAvailableQuality(
            @PathVariable Long movieId,
            @RequestParam(required = false) String preferredQuality) {
        try {
            String response = videoResolutionService.getBestAvailableQuality(movieId, preferredQuality);
            ApiResponse<String> apiResponse = new ApiResponse<>("SUCCESS", "Best available quality retrieved", response);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error getting best available quality", e);
            ApiResponse<String> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/movie/{movieId}/min-height/{minHeight}")
    public ResponseEntity<ApiResponse<List<AvailableResolutionResponse>>> getResolutionsByMinHeight(
            @PathVariable Long movieId,
            @PathVariable Integer minHeight) {
        try {
            List<AvailableResolutionResponse> response = videoResolutionService.getResolutionsByMinHeight(movieId, minHeight);
            ApiResponse<List<AvailableResolutionResponse>> apiResponse = new ApiResponse<>("SUCCESS", "Resolutions retrieved successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error getting resolutions by min height", e);
            ApiResponse<List<AvailableResolutionResponse>> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
}
