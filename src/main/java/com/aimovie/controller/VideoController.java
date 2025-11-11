package com.aimovie.controller;

import com.aimovie.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
@Slf4j
public class VideoController {

    private final FileUploadService fileUploadService;

    @GetMapping(value = "/test/{movieId}/{filename}")
    public ResponseEntity<String> testVideoEndpoint(@PathVariable String movieId, @PathVariable String filename) {
        log.info("Test endpoint called: movieId={}, filename={}", movieId, filename);
        
        Path videoPath = Paths.get("uploads/videos", movieId, filename);
        boolean exists = Files.exists(videoPath);
        
        String response = String.format("MovieId: %s, Filename: %s, Path: %s, Exists: %s", 
                movieId, filename, videoPath.toString(), exists);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/stream/{movieId}/{filename}")
    public ResponseEntity<Resource> streamVideoWithSubdir(@PathVariable String movieId,
                                                          @PathVariable String filename,
                                                          @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader) {
        log.info("Streaming video request: movieId={}, filename={}", movieId, filename);
        try {
            Path videoPath = Paths.get("uploads/videos", movieId, filename);
            
            if (!Files.exists(videoPath)) {
                log.warn("Video file not found: {}/{}", movieId, filename);
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(videoPath);
            
            String contentType = "video/mp4"; 
            String fileExtension = filename.toLowerCase();
            if (fileExtension.endsWith(".mkv")) {
                contentType = "video/x-matroska";
            } else if (fileExtension.endsWith(".avi")) {
                contentType = "video/x-msvideo";
            } else if (fileExtension.endsWith(".mov")) {
                contentType = "video/quicktime";
            } else if (fileExtension.endsWith(".wmv")) {
                contentType = "video/x-ms-wmv";
            } else if (fileExtension.endsWith(".flv")) {
                contentType = "video/x-flv";
            } else if (fileExtension.endsWith(".webm")) {
                contentType = "video/webm";
            }

            long fileSize = Files.size(videoPath);
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
            headers.set(HttpHeaders.CACHE_CONTROL, "public, max-age=3600");
            headers.setContentType(MediaType.parseMediaType(contentType));

            // Handle HTTP Range requests (e.g., for HTML5 video tag seeking)
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String rangeValue = rangeHeader.substring("bytes=".length());
                String[] ranges = rangeValue.split("-");
                long start = 0;
                long end = fileSize - 1;
                try {
                    if (!ranges[0].isEmpty()) {
                        start = Long.parseLong(ranges[0]);
                    }
                    if (ranges.length > 1 && !ranges[1].isEmpty()) {
                        end = Long.parseLong(ranges[1]);
                    }
                } catch (NumberFormatException ignored) {
                }

                if (start > end || start >= fileSize) {
                    headers.set(HttpHeaders.CONTENT_RANGE, "bytes */" + fileSize);
                    return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                            .headers(headers)
                            .build();
                }

                end = Math.min(end, fileSize - 1);
                long contentLength = end - start + 1;

                byte[] data;
                try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(videoPath.toFile(), "r")) {
                    raf.seek(start);
                    data = new byte[(int) contentLength];
                    int read = raf.read(data);
                    if (read < contentLength) {
                        data = java.util.Arrays.copyOf(data, read);
                        contentLength = read;
                    }
                }

                headers.set(HttpHeaders.CONTENT_RANGE, String.format("bytes %d-%d/%d", start, start + contentLength - 1, fileSize));
                headers.setContentLength(contentLength);

                log.info("Streaming video range: {}/{} ({}-{} of {}, content-type: {})",
                        movieId, filename, start, start + contentLength - 1, fileSize, contentType);

                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .headers(headers)
                        .body(new org.springframework.core.io.ByteArrayResource(data));
            }

            // No Range header: return the whole file
            headers.setContentLength(fileSize);
            log.info("Streaming video: {}/{} ({} bytes, content-type: {})", movieId, filename, fileSize, contentType);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (IOException e) {
            log.error("Error streaming video: {}/{}", movieId, filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(value = "/stream/{filename}")
    public ResponseEntity<Resource> streamVideo(@PathVariable String filename,
                                                @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader) {
        try {
            Path videoPath = fileUploadService.getVideoFilePath(filename);
            
            if (!Files.exists(videoPath)) {
                log.warn("Video file not found: {}", filename);
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(videoPath);
            
            String contentType = "video/mp4"; 
            String fileExtension = filename.toLowerCase();
            if (fileExtension.endsWith(".mkv")) {
                contentType = "video/x-matroska";
            } else if (fileExtension.endsWith(".avi")) {
                contentType = "video/x-msvideo";
            } else if (fileExtension.endsWith(".mov")) {
                contentType = "video/quicktime";
            } else if (fileExtension.endsWith(".wmv")) {
                contentType = "video/x-ms-wmv";
            } else if (fileExtension.endsWith(".flv")) {
                contentType = "video/x-flv";
            } else if (fileExtension.endsWith(".webm")) {
                contentType = "video/webm";
            }

            long fileSize = Files.size(videoPath);
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
            headers.set(HttpHeaders.CACHE_CONTROL, "public, max-age=3600");
            headers.setContentType(MediaType.parseMediaType(contentType));

            // Handle HTTP Range requests (e.g., for HTML5 video tag seeking)
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String rangeValue = rangeHeader.substring("bytes=".length());
                String[] ranges = rangeValue.split("-");
                long start = 0;
                long end = fileSize - 1;
                try {
                    if (!ranges[0].isEmpty()) {
                        start = Long.parseLong(ranges[0]);
                    }
                    if (ranges.length > 1 && !ranges[1].isEmpty()) {
                        end = Long.parseLong(ranges[1]);
                    }
                } catch (NumberFormatException ignored) {
                }

                if (start > end || start >= fileSize) {
                    headers.set(HttpHeaders.CONTENT_RANGE, "bytes */" + fileSize);
                    return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                            .headers(headers)
                            .build();
                }

                end = Math.min(end, fileSize - 1);
                long contentLength = end - start + 1;

                byte[] data;
                try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(videoPath.toFile(), "r")) {
                    raf.seek(start);
                    data = new byte[(int) contentLength];
                    int read = raf.read(data);
                    if (read < contentLength) {
                        data = java.util.Arrays.copyOf(data, read);
                        contentLength = read;
                    }
                }

                headers.set(HttpHeaders.CONTENT_RANGE, String.format("bytes %d-%d/%d", start, start + contentLength - 1, fileSize));
                headers.setContentLength(contentLength);

                log.info("Streaming video range: {} ({}-{} of {}, content-type: {})",
                        filename, start, start + contentLength - 1, fileSize, contentType);

                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .headers(headers)
                        .body(new org.springframework.core.io.ByteArrayResource(data));
            }

            // No Range header: return the whole file
            headers.setContentLength(fileSize);
            log.info("Streaming video: {} ({} bytes, content-type: {})", filename, fileSize, contentType);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (IOException e) {
            log.error("Error streaming video: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/info/{filename}")
    public ResponseEntity<VideoInfo> getVideoInfo(@PathVariable String filename) {
        try {
            if (!fileUploadService.fileExists(filename)) {
                return ResponseEntity.notFound().build();
            }

            long fileSize = fileUploadService.getFileSize(filename);
            Path videoPath = fileUploadService.getVideoFilePath(filename);
            String contentType = Files.probeContentType(videoPath);
            
            VideoInfo info = new VideoInfo(filename, fileSize, contentType);
            
            return ResponseEntity.ok(info);

        } catch (IOException e) {
            log.error("Error getting video info: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public static class VideoInfo {
        private String filename;
        private long fileSize;
        private String contentType;

        public VideoInfo(String filename, long fileSize, String contentType) {
            this.filename = filename;
            this.fileSize = fileSize;
            this.contentType = contentType;
        }

        public String getFilename() { return filename; }
        public long getFileSize() { return fileSize; }
        public String getContentType() { return contentType; }
    }
}
