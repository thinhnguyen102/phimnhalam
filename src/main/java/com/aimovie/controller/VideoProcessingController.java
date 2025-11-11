package com.aimovie.controller;

import com.aimovie.dto.ApiResponse;
import com.aimovie.dto.VideoProcessingResult;
import com.aimovie.service.FFmpegService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/video-processing")
@RequiredArgsConstructor
@Slf4j
public class VideoProcessingController {

    private final FFmpegService ffmpegService;

    @Value("${app.video.upload-dir:uploads/videos}")
    private String videoUploadDir;

    @Value("${app.video.temp-dir:uploads/temp}")
    private String tempDir;

    @PostMapping("/upload-and-process/{movieId}")
    public ResponseEntity<ApiResponse<VideoProcessingResult>> uploadAndProcessVideo(
            @PathVariable Long movieId,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                ApiResponse<VideoProcessingResult> apiResponse = new ApiResponse<>("ERROR", "File is empty", null);
                return ResponseEntity.badRequest().body(apiResponse);
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                ApiResponse<VideoProcessingResult> apiResponse = new ApiResponse<>("ERROR", "File must be a video", null);
                return ResponseEntity.badRequest().body(apiResponse);
            }

            // Create temp directory
            Path tempPath = Paths.get(tempDir);
            Files.createDirectories(tempPath);

            // Generate unique filename
            String originalFileName = file.getOriginalFilename();
            String fileExtension = originalFileName != null ? 
                    originalFileName.substring(originalFileName.lastIndexOf('.')) : ".mp4";
            String tempFileName = UUID.randomUUID().toString() + fileExtension;
            Path tempFilePath = tempPath.resolve(tempFileName);

            // Save uploaded file to temp location
            file.transferTo(tempFilePath.toFile());
            log.info("Video uploaded to temp location: {}", tempFilePath);

            // Start async processing
            ffmpegService.processVideoToMultipleResolutions(tempFilePath, movieId, originalFileName);

            // Return immediate response with processing status
            VideoProcessingResult initialResult = VideoProcessingResult.builder()
                    .success(true)
                    .message("Video upload successful, processing started")
                    .movieId(movieId)
                    .originalFileName(originalFileName)
                    .status("PROCESSING")
                    .build();

            ApiResponse<VideoProcessingResult> apiResponse = new ApiResponse<>("SUCCESS", "Video processing started", initialResult);
            return ResponseEntity.accepted().body(apiResponse);

        } catch (Exception e) {
            log.error("Error uploading and processing video: {}", e.getMessage());
            ApiResponse<VideoProcessingResult> apiResponse = new ApiResponse<>("ERROR", "Failed to upload and process video: " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @PostMapping("/process-existing/{movieId}")
    public ResponseEntity<ApiResponse<VideoProcessingResult>> processExistingVideo(
            @PathVariable Long movieId,
            @RequestParam String videoPath) {
        try {
            Path inputPath = Paths.get(videoPath);
            if (!Files.exists(inputPath)) {
                ApiResponse<VideoProcessingResult> apiResponse = new ApiResponse<>("ERROR", "Video file not found", null);
                return ResponseEntity.badRequest().body(apiResponse);
            }

            ffmpegService.processVideoToMultipleResolutions(inputPath, movieId, inputPath.getFileName().toString());

            VideoProcessingResult initialResult = VideoProcessingResult.builder()
                    .success(true)
                    .message("Video processing started")
                    .movieId(movieId)
                    .originalFileName(inputPath.getFileName().toString())
                    .status("PROCESSING")
                    .build();

            ApiResponse<VideoProcessingResult> apiResponse = new ApiResponse<>("SUCCESS", "Video processing started", initialResult);
            return ResponseEntity.accepted().body(apiResponse);

        } catch (Exception e) {
            log.error("Error processing existing video: {}", e.getMessage());
            ApiResponse<VideoProcessingResult> apiResponse = new ApiResponse<>("ERROR", "Failed to process video: " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/status/{movieId}")
    public ResponseEntity<ApiResponse<Object>> getProcessingStatus(@PathVariable Long movieId) {
        try {
            // This would typically check a job queue or database for processing status
            // For now, return a simple status
            ApiResponse<Object> apiResponse = new ApiResponse<>("SUCCESS", "Processing status retrieved", 
                    "Processing status for movie " + movieId);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error getting processing status: {}", e.getMessage());
            ApiResponse<Object> apiResponse = new ApiResponse<>("ERROR", "Failed to get processing status", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/ffmpeg-status")
    public ResponseEntity<ApiResponse<Boolean>> getFFmpegStatus() {
        try {
            boolean isAvailable = ffmpegService.isFFmpegAvailable();
            String message = isAvailable ? "FFmpeg is available" : "FFmpeg is not available";
            ApiResponse<Boolean> apiResponse = new ApiResponse<>("SUCCESS", message, isAvailable);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error checking FFmpeg status: {}", e.getMessage());
            ApiResponse<Boolean> apiResponse = new ApiResponse<>("ERROR", "Failed to check FFmpeg status", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @PostMapping("/encode-single/{movieId}")
    public ResponseEntity<ApiResponse<VideoProcessingResult>> encodeToSingleResolution(
            @PathVariable Long movieId,
            @RequestParam String inputPath,
            @RequestParam String quality,
            @RequestParam int width,
            @RequestParam int height,
            @RequestParam int bitrate) {
        try {
            Path inputFilePath = Paths.get(inputPath);
            if (!Files.exists(inputFilePath)) {
                ApiResponse<VideoProcessingResult> apiResponse = new ApiResponse<>("ERROR", "Input video file not found", null);
                return ResponseEntity.badRequest().body(apiResponse);
            }

            String outputFileName = String.format("%s_%s.mp4", movieId, quality);
            Path outputPath = Paths.get(videoUploadDir, movieId.toString(), outputFileName);
            Files.createDirectories(outputPath.getParent());

            CompletableFuture<VideoProcessingResult> result = ffmpegService.encodeVideoToResolution(
                    inputFilePath, outputPath.toString(), quality, width, height, bitrate);

            VideoProcessingResult processingResult = result.get(); // Wait for completion

            ApiResponse<VideoProcessingResult> apiResponse = new ApiResponse<>("SUCCESS", "Single resolution encoding completed", processingResult);
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            log.error("Error encoding to single resolution: {}", e.getMessage());
            ApiResponse<VideoProcessingResult> apiResponse = new ApiResponse<>("ERROR", "Failed to encode video: " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @DeleteMapping("/cleanup-temp")
    public ResponseEntity<ApiResponse<Void>> cleanupTempFiles() {
        try {
            Path tempPath = Paths.get(tempDir);
            ffmpegService.cleanupTempFiles(tempPath);
            
            ApiResponse<Void> apiResponse = new ApiResponse<>("SUCCESS", "Temp files cleaned up", null);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error cleaning up temp files: {}", e.getMessage());
            ApiResponse<Void> apiResponse = new ApiResponse<>("ERROR", "Failed to cleanup temp files", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
}
