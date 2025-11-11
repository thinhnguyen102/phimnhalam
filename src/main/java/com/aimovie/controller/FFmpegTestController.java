package com.aimovie.controller;

import com.aimovie.dto.ApiResponse;
import com.aimovie.service.FFmpegService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/ffmpeg-test")
@RequiredArgsConstructor
@Slf4j
public class FFmpegTestController {

    private final FFmpegService ffmpegService;

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Boolean>> checkFFmpegStatus() {
        try {
            boolean isAvailable = ffmpegService.isFFmpegAvailable();
            String message = isAvailable ? "FFmpeg is available and ready" : "FFmpeg is not available";
            ApiResponse<Boolean> apiResponse = new ApiResponse<>("SUCCESS", message, isAvailable);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error checking FFmpeg status: {}", e.getMessage());
            ApiResponse<Boolean> apiResponse = new ApiResponse<>("ERROR", "Failed to check FFmpeg status: " + e.getMessage(), false);
            return ResponseEntity.ok(apiResponse);
        }
    }

    @GetMapping("/test-video-info")
    public ResponseEntity<ApiResponse<Object>> testVideoInfo(@RequestParam String videoPath) {
        try {
            Path path = Paths.get(videoPath);
            if (!Files.exists(path)) {
                ApiResponse<Object> apiResponse = new ApiResponse<>("ERROR", "Video file not found", null);
                return ResponseEntity.ok(apiResponse);
            }

            var result = ffmpegService.getVideoMetadata(path);
            ApiResponse<Object> apiResponse = new ApiResponse<>("SUCCESS", "Video metadata extracted", result);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error testing video info: {}", e.getMessage());
            ApiResponse<Object> apiResponse = new ApiResponse<>("ERROR", "Failed to extract video info: " + e.getMessage(), null);
            return ResponseEntity.ok(apiResponse);
        }
    }

    @PostMapping("/test-encoding")
    public ResponseEntity<ApiResponse<Object>> testEncoding(
            @RequestParam String inputPath,
            @RequestParam String outputPath,
            @RequestParam(defaultValue = "720p") String quality,
            @RequestParam(defaultValue = "1280") int width,
            @RequestParam(defaultValue = "720") int height,
            @RequestParam(defaultValue = "2500") int bitrate) {
        try {
            Path inputFilePath = Paths.get(inputPath);
            if (!Files.exists(inputFilePath)) {
                ApiResponse<Object> apiResponse = new ApiResponse<>("ERROR", "Input video file not found", null);
                return ResponseEntity.ok(apiResponse);
            }

            var result = ffmpegService.encodeVideoToResolution(
                    inputFilePath, outputPath, quality, width, height, bitrate);

            ApiResponse<Object> apiResponse = new ApiResponse<>("SUCCESS", "Encoding test completed", result);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error testing encoding: {}", e.getMessage());
            ApiResponse<Object> apiResponse = new ApiResponse<>("ERROR", "Failed to test encoding: " + e.getMessage(), null);
            return ResponseEntity.ok(apiResponse);
        }
    }
}
