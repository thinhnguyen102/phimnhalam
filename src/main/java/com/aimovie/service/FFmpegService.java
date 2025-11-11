package com.aimovie.service;

import com.aimovie.dto.VideoProcessingResult;
import com.aimovie.entity.VideoResolution;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface FFmpegService {

    CompletableFuture<VideoProcessingResult> processVideoToMultipleResolutions(
            Path inputVideoPath, 
            Long movieId, 
            String originalFileName);

    CompletableFuture<VideoProcessingResult> encodeVideoToResolution(
            Path inputVideoPath, 
            String outputPath, 
            String quality, 
            int width, 
            int height, 
            int bitrate);

    VideoProcessingResult getVideoMetadata(Path videoPath);

    boolean isFFmpegAvailable();

    List<VideoResolution> createVideoResolutionsForMovie(Long movieId, String baseFileName);

    void cleanupTempFiles(Path tempPath);

    CompletableFuture<Void> processVideoAsync(Long movieId, Path inputVideoPath, String originalFileName);
}
