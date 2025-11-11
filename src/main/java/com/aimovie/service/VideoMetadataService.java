package com.aimovie.service;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
@Slf4j
public class VideoMetadataService {

    public VideoMetadata extractMetadata(MultipartFile file) throws IOException {
        Path tempFile = Files.createTempFile("video_", "_" + file.getOriginalFilename());
        
        try {
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            
            return extractMetadataFromFile(tempFile.toFile(), file.getOriginalFilename());
            
        } finally {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException e) {
                log.warn("Failed to delete temporary file: {}", tempFile, e);
            }
        }
    }

    public VideoMetadata extractMetadataFromFile(File file, String originalFilename) throws IOException {
        VideoMetadata metadata = new VideoMetadata();
        
        String format = getFileExtension(originalFilename);
        metadata.setVideoFormat(format);
        
        long fileSize = file.length();
        metadata.setFileSizeBytes(fileSize);
        
        // Try to extract real metadata using FFmpeg
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file)) {
            grabber.start();
            
            int width = grabber.getImageWidth();
            int height = grabber.getImageHeight();
            double frameRate = grabber.getFrameRate();
            long durationInMicroseconds = grabber.getLengthInTime();
            double durationInSeconds = durationInMicroseconds / 1_000_000.0;
            
            metadata.setWidth(width);
            metadata.setHeight(height);
            metadata.setFrameRate(frameRate);
            metadata.setDuration(durationInSeconds);
            metadata.setVideoQuality(determineVideoQuality(width, height));
            metadata.setBitRate(estimateBitRate(fileSize, durationInSeconds));
            metadata.setCodec(27); // H.264
            
            log.info("Extracted video metadata (FFmpeg): {}x{}, {}fps, {}s, {}bps, quality: {}", 
                width, height, frameRate, durationInSeconds, metadata.getBitRate(), metadata.getVideoQuality());
            
        } catch (Exception e) {
            log.warn("Failed to extract metadata with FFmpeg, using estimation: {}", e.getMessage());
            
            // Fallback to estimation if FFmpeg fails
            metadata.setVideoQuality(estimateVideoQuality(fileSize, format));
            metadata.setWidth(1920);  
            metadata.setHeight(1080); 
            metadata.setFrameRate(24.0); 
            metadata.setDuration(estimateDuration(fileSize)); 
            metadata.setBitRate(estimateBitRate(fileSize, metadata.getDuration()));
            metadata.setCodec(27);
            
            log.info("Extracted video metadata (estimated): {}x{}, {}fps, {}s, {}bps, quality: {}", 
                metadata.getWidth(), metadata.getHeight(), metadata.getFrameRate(), 
                metadata.getDuration(), metadata.getBitRate(), metadata.getVideoQuality());
        }
        
        return metadata;
    }

    private String determineVideoQuality(int width, int height) {
        if (width >= 3840 || height >= 2160) {
            return "4K";
        } else if (width >= 1920 || height >= 1080) {
            return "1080p";
        } else if (width >= 1280 || height >= 720) {
            return "720p";
        } else if (width >= 854 || height >= 480) {
            return "480p";
        } else if (width >= 640 || height >= 360) {
            return "360p";
        } else {
            return "240p";
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private String estimateVideoQuality(long fileSize, String format) {
        long sizeInMB = fileSize / (1024 * 1024);
        
        if (sizeInMB > 2000) { 
            return "4K";
        } else if (sizeInMB > 1000) { 
            return "1080p";
        } else if (sizeInMB > 500) { 
            return "720p";
        } else if (sizeInMB > 200) { 
            return "480p";
        } else {
            return "360p";
        }
    }

    private double estimateDuration(long fileSize) {
        long sizeInBits = fileSize * 8;
        double estimatedSeconds = sizeInBits / (2.0 * 1024 * 1024); 
        return Math.max(60, estimatedSeconds); 
    }

    private int estimateBitRate(long fileSize, double duration) {
        if (duration <= 0) return 2000000; 
        long sizeInBits = fileSize * 8;
        return (int) (sizeInBits / duration);
    }

    public static class VideoMetadata {
        private int width;
        private int height;
        private double frameRate;
        private double duration; 
        private int bitRate;
        private int codec;
        private String videoQuality;
        private String videoFormat;
        private long fileSizeBytes;

        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }

        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }

        public double getFrameRate() { return frameRate; }
        public void setFrameRate(double frameRate) { this.frameRate = frameRate; }

        public double getDuration() { return duration; }
        public void setDuration(double duration) { this.duration = duration; }

        public int getBitRate() { return bitRate; }
        public void setBitRate(int bitRate) { this.bitRate = bitRate; }

        public int getCodec() { return codec; }
        public void setCodec(int codec) { this.codec = codec; }

        public String getVideoQuality() { return videoQuality; }
        public void setVideoQuality(String videoQuality) { this.videoQuality = videoQuality; }

        public String getVideoFormat() { return videoFormat; }
        public void setVideoFormat(String videoFormat) { this.videoFormat = videoFormat; }

        public long getFileSizeBytes() { return fileSizeBytes; }
        public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

        public int getDurationInMinutes() {
            return (int) Math.round(duration / 60.0);
        }

        public int getDurationInSeconds() {
            return (int) Math.round(duration);
        }

        public String getResolution() {
            return width + "x" + height;
        }

        @Override
        public String toString() {
            return String.format("VideoMetadata{resolution=%dx%d, fps=%.2f, duration=%.2fs, quality=%s, format=%s, size=%d bytes}",
                width, height, frameRate, duration, videoQuality, videoFormat, fileSizeBytes);
        }
    }
}
