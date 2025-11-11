package com.aimovie.serviceImpl;

import com.aimovie.dto.ProcessedVideoInfo;
import com.aimovie.dto.VideoProcessingResult;
import com.aimovie.entity.Movie;
import com.aimovie.entity.VideoResolution;
import com.aimovie.repository.MovieRepository;
import com.aimovie.repository.VideoResolutionRepository;
import com.aimovie.service.FFmpegService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
// ff
@Service
@RequiredArgsConstructor
@Slf4j
public class FFmpegServiceImpl implements FFmpegService {

    private final VideoResolutionRepository videoResolutionRepository;
    private final MovieRepository movieRepository;

    @Value("${app.video.upload-dir:uploads/videos}")
    private String videoUploadDir;

    @Value("${app.video.temp-dir:uploads/temp}")
    private String tempDir;

    // Video quality configurations
    private static final List<VideoQualityConfig> QUALITY_CONFIGS = List.of(
            new VideoQualityConfig("360p", 640, 360, 800),
            new VideoQualityConfig("720p", 1280, 720, 2500),
            new VideoQualityConfig("1080p", 1920, 1080, 5000)
    );

    @Override
    public boolean isFFmpegAvailable() {
        try {
            // Try to initialize FFmpeg
            avutil.av_log_set_level(avutil.AV_LOG_QUIET);
            return true;
        } catch (Exception e) {
            log.error("FFmpeg is not available: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public VideoProcessingResult getVideoMetadata(Path videoPath) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath.toFile())) {
            grabber.start();
            
            int width = grabber.getImageWidth();
            int height = grabber.getImageHeight();
            long duration = grabber.getLengthInTime() / 1000000; // Convert to seconds
            double frameRate = grabber.getFrameRate();
            String format = grabber.getFormat();
            
            log.info("Video metadata - Width: {}, Height: {}, Duration: {}s, FrameRate: {}, Format: {}", 
                    width, height, duration, frameRate, format);
            
            return VideoProcessingResult.builder()
                    .success(true)
                    .message("Video metadata extracted successfully")
                    .build();
                    
        } catch (Exception e) {
            log.error("Error extracting video metadata: {}", e.getMessage());
            return VideoProcessingResult.builder()
                    .success(false)
                    .message("Failed to extract video metadata")
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    @Override
    @Async
    public CompletableFuture<VideoProcessingResult> processVideoToMultipleResolutions(
            Path inputVideoPath, Long movieId, String originalFileName) {
        
        long startTime = System.currentTimeMillis();
        List<ProcessedVideoInfo> processedVideos = new ArrayList<>();
        
        try {
            log.info("Starting video processing for movie ID: {}, file: {}", movieId, originalFileName);
            
            // Create output directory
            Path outputDir = Paths.get(videoUploadDir, movieId.toString());
            Files.createDirectories(outputDir);
            
            // Process each quality
            for (VideoQualityConfig config : QUALITY_CONFIGS) {
                try {
                    String outputFileName = String.format("%s_%s.mp4", movieId, config.quality);
                    Path outputPath = outputDir.resolve(outputFileName);
                    
                    log.info("Processing {} quality for movie {}", config.quality, movieId);
                    
                    ProcessedVideoInfo result = encodeVideoToResolutionSync(
                            inputVideoPath, 
                            outputPath, 
                            config.quality, 
                            config.width, 
                            config.height, 
                            config.bitrate
                    );
                    
                    processedVideos.add(result);
                    
                    if (result.isSuccess()) {
                        // Create VideoResolution entity
                        createVideoResolutionEntity(movieId, config, outputPath, result.getFileSizeBytes());
                    }
                    
                } catch (Exception e) {
                    log.error("Error processing {} quality: {}", config.quality, e.getMessage());
                    processedVideos.add(ProcessedVideoInfo.builder()
                            .quality(config.quality)
                            .success(false)
                            .errorMessage(e.getMessage())
                            .build());
                }
            }
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Update movie's available qualities after successful processing
            updateMovieAvailableQualities(movieId);
            
            return CompletableFuture.completedFuture(VideoProcessingResult.builder()
                    .success(true)
                    .message("Video processing completed")
                    .movieId(movieId)
                    .originalFileName(originalFileName)
                    .processedVideos(processedVideos)
                    .processingTimeMs(processingTime)
                    .status("COMPLETED")
                    .build());
                    
        } catch (Exception e) {
            log.error("Error in video processing: {}", e.getMessage());
            long processingTime = System.currentTimeMillis() - startTime;
            
            return CompletableFuture.completedFuture(VideoProcessingResult.builder()
                    .success(false)
                    .message("Video processing failed")
                    .movieId(movieId)
                    .originalFileName(originalFileName)
                    .errorMessage(e.getMessage())
                    .processingTimeMs(processingTime)
                    .status("FAILED")
                    .build());
        }
    }

    @Override
    @Async
    public CompletableFuture<VideoProcessingResult> encodeVideoToResolution(
            Path inputVideoPath, String outputPath, String quality, int width, int height, int bitrate) {
        
        try {
            ProcessedVideoInfo result = encodeVideoToResolutionSync(
                    inputVideoPath, 
                    Paths.get(outputPath), 
                    quality, 
                    width, 
                    height, 
                    bitrate
            );
            
            return CompletableFuture.completedFuture(VideoProcessingResult.builder()
                    .success(result.isSuccess())
                    .message(result.isSuccess() ? "Encoding completed" : "Encoding failed")
                    .errorMessage(result.getErrorMessage())
                    .build());
                    
        } catch (Exception e) {
            return CompletableFuture.completedFuture(VideoProcessingResult.builder()
                    .success(false)
                    .message("Encoding failed")
                    .errorMessage(e.getMessage())
                    .build());
        }
    }

    private ProcessedVideoInfo encodeVideoToResolutionSync(
            Path inputVideoPath, Path outputPath, String quality, int width, int height, int bitrate) {
        
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputVideoPath.toFile());
             FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputPath.toFile(), width, height)) {
            
            grabber.start();
            
            // Configure recorder (video + audio)
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setFormat("mp4");
            recorder.setFrameRate(grabber.getFrameRate());
            recorder.setVideoBitrate(bitrate * 1000); // Convert to bits per second
            recorder.setVideoQuality(0); // Best quality
            
            // Set pixel format
            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);

            // Audio settings
            int audioChannels = Math.max(1, grabber.getAudioChannels());
            int sampleRate = Math.max(44100, grabber.getSampleRate());
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
            recorder.setAudioBitrate(128 * 1000);
            recorder.setSampleRate(sampleRate);
            recorder.setAudioChannels(audioChannels);
            
            recorder.start();
            
            Frame frame;
            int frameCount = 0;
            
            while ((frame = grabber.grab()) != null) {
                if (frame.image != null) {
                    recorder.record(frame);
                    frameCount++;
                    if (frameCount % 100 == 0) {
                        log.debug("Processed {} frames for {} quality", frameCount, quality);
                    }
                } else if (frame.samples != null) {
                    recorder.recordSamples(frame.samples);
                }
            }
            
            recorder.stop();
            grabber.stop();
            
            long fileSize = Files.size(outputPath);
            
            log.info("Successfully encoded {} quality: {} frames, {} bytes", quality, frameCount, fileSize);
            
            return ProcessedVideoInfo.builder()
                    .quality(quality)
                    .outputPath(outputPath.toString())
                    .fileSizeBytes(fileSize)
                    .width(width)
                    .height(height)
                    .bitrate(bitrate)
                    .format("mp4")
                    .success(true)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error encoding video to {} quality: {}", quality, e.getMessage());
            return ProcessedVideoInfo.builder()
                    .quality(quality)
                    .outputPath(outputPath.toString())
                    .width(width)
                    .height(height)
                    .bitrate(bitrate)
                    .format("mp4")
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    private void createVideoResolutionEntity(Long movieId, VideoQualityConfig config, Path outputPath, long fileSize) {
        try {
            VideoResolution videoResolution = VideoResolution.builder()
                    .movie(movieRepository.findById(movieId).orElse(null))
                    .quality(config.quality)
                    .width(config.width)
                    .height(config.height)
                    .videoUrl("/api/videos/stream/" + movieId + "/" + outputPath.getFileName().toString())
                    .videoFormat("mp4")
                    .fileSizeBytes(fileSize)
                    .bitrate(config.bitrate)
                    .isAvailable(true)
                    .encodingStatus("COMPLETED")
                    .encodingProgress(100)
                    .build();
            
            videoResolutionRepository.save(videoResolution);
            log.info("Created VideoResolution entity for movie {} quality {} with URL: {}", movieId, config.quality, videoResolution.getVideoUrl());
            
        } catch (Exception e) {
            log.error("Error creating VideoResolution entity: {}", e.getMessage());
        }
    }

    @Override
    public List<VideoResolution> createVideoResolutionsForMovie(Long movieId, String baseFileName) {
        List<VideoResolution> resolutions = new ArrayList<>();
        
        for (VideoQualityConfig config : QUALITY_CONFIGS) {
            String fileName = String.format("%s_%s.mp4", baseFileName, config.quality);
            Path outputPath = Paths.get(videoUploadDir, movieId.toString(), fileName);
            
            if (Files.exists(outputPath)) {
                try {
                    long fileSize = Files.size(outputPath);
                    
                    VideoResolution resolution = VideoResolution.builder()
                            .movie(movieRepository.findById(movieId).orElse(null))
                            .quality(config.quality)
                            .width(config.width)
                            .height(config.height)
                            .videoUrl("/api/videos/stream/" + movieId + "/" + fileName)
                            .videoFormat("mp4")
                            .fileSizeBytes(fileSize)
                            .bitrate(config.bitrate)
                            .isAvailable(true)
                            .encodingStatus("COMPLETED")
                            .encodingProgress(100)
                            .build();
                    
                    VideoResolution savedResolution = videoResolutionRepository.save(resolution);
                    resolutions.add(savedResolution);
                    log.info("Saved VideoResolution entity for movie {} quality {}", movieId, config.quality);
                    
                } catch (Exception e) {
                    log.error("Error creating VideoResolution for {}: {}", config.quality, e.getMessage());
                }
            }
        }
        
        return resolutions;
    }

    @Override
    public void cleanupTempFiles(Path tempPath) {
        try {
            if (Files.exists(tempPath)) {
                Files.walk(tempPath)
                        .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (Exception e) {
                                log.warn("Could not delete temp file: {}", path);
                            }
                        });
                log.info("Cleaned up temp files in: {}", tempPath);
            }
        } catch (Exception e) {
            log.error("Error cleaning up temp files: {}", e.getMessage());
        }
    }

    private void updateMovieAvailableQualities(Long movieId) {
        try {
            List<String> availableQualities = videoResolutionRepository.findAvailableQualitiesByMovieId(movieId);
            if (!availableQualities.isEmpty()) {
                Movie movie = movieRepository.findById(movieId).orElse(null);
                if (movie != null) {
                    movie.setAvailableQualities(availableQualities);
                    movieRepository.save(movie);
                    log.info("Updated available qualities for movie {}: {}", movieId, availableQualities);
                }
            }
        } catch (Exception e) {
            log.error("Error updating available qualities for movie {}: {}", movieId, e.getMessage());
        }
    }

    @Override
    @Async
    public CompletableFuture<Void> processVideoAsync(Long movieId, Path inputVideoPath, String originalFileName) {
        return processVideoToMultipleResolutions(inputVideoPath, movieId, originalFileName)
                .thenAccept(result -> {
                    if (result.isSuccess()) {
                        log.info("Async video processing completed for movie {}", movieId);
                    } else {
                        log.error("Async video processing failed for movie {}: {}", movieId, result.getErrorMessage());
                    }
                });
    }

    // Helper class for video quality configuration
    private static class VideoQualityConfig {
        final String quality;
        final int width;
        final int height;
        final int bitrate;

        VideoQualityConfig(String quality, int width, int height, int bitrate) {
            this.quality = quality;
            this.width = width;
            this.height = height;
            this.bitrate = bitrate;
        }
    }
}
