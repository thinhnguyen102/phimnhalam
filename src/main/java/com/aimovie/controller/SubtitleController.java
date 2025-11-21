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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/subtitles")
@RequiredArgsConstructor
@Slf4j
public class SubtitleController {

    private final FileUploadService fileUploadService;

    @GetMapping("/stream/{filename}")
    public ResponseEntity<Resource> streamSubtitle(@PathVariable String filename) {
        try {
            Path subtitlePath = fileUploadService.getSubtitleFilePath(filename);
            if (!Files.exists(subtitlePath)) {
                return ResponseEntity.notFound().build();
            }
            Resource resource = new FileSystemResource(subtitlePath);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(resolveContentType(filename));
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"");
            headers.set(HttpHeaders.CACHE_CONTROL, "public, max-age=3600");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (Exception e) {
            log.error("Error streaming subtitle {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private MediaType resolveContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".vtt")) {
            return MediaType.parseMediaType("text/vtt");
        }
        if (lower.endsWith(".srt")) {
            return MediaType.TEXT_PLAIN;
        }
        if (lower.endsWith(".ass") || lower.endsWith(".ssa") || lower.endsWith(".sub")) {
            return MediaType.TEXT_PLAIN;
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}

