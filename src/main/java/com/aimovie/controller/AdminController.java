package com.aimovie.controller;

import com.aimovie.dto.*;
import com.aimovie.entity.Report;
import com.aimovie.service.AdminService;
import com.aimovie.service.CountryService;
import com.aimovie.service.FileUploadService;
import com.aimovie.service.FFmpegService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
public class AdminController {

    private final AdminService adminService;
    private final CountryService countryService;
    private final FileUploadService fileUploadService;
    private final FFmpegService ffmpegService;


    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardDTO> getAdminDashboard() {
        try {
            AdminDashboardDTO dashboard = adminService.getAdminDashboard();
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            log.error("Error getting admin dashboard", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminStatsDTO> getAdminStats() {
        try {
            AdminStatsDTO stats = adminService.getAdminStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting admin stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/stats/monthly/{year}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MonthlyStatsDTO>> getMonthlyStats(@PathVariable int year) {
        try {
            List<MonthlyStatsDTO> monthlyStats = adminService.getMonthlyStats(year);
            return ResponseEntity.ok(monthlyStats);
        } catch (Exception e) {
            log.error("Error getting monthly stats for year: {}", year, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AdminUserDTO>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        try {
            Page<AdminUserDTO> users = adminService.getAllUsers(pageable);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error getting all users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminUserDTO> getUserById(@PathVariable Long userId) {
        try {
            AdminUserDTO user = adminService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            log.error("User not found with id: {}", userId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting user by id: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/users/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminUserDTO> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam boolean enabled) {
        try {
            AdminUserDTO user = adminService.updateUserStatus(userId, enabled);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            log.error("User not found with id: {}", userId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating user status for id: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/users/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminUserDTO> updateUserRoles(
            @PathVariable Long userId,
            @RequestBody List<String> roles) {
        try {
            AdminUserDTO user = adminService.updateUserRoles(userId, roles);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            log.error("User not found with id: {}", userId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating user roles for id: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        try {
            adminService.deleteUser(userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("User not found with id: {}", userId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting user with id: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/movies")
    public ResponseEntity<Page<AdminMovieDTO>> getAllMovies(
            @PageableDefault(size = 20) Pageable pageable) {
        try {
            Page<AdminMovieDTO> movies = adminService.getAllMovies(pageable);
            return ResponseEntity.ok(movies);
        } catch (Exception e) {
            log.error("Error getting all movies", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/movies/{movieId}")
    public ResponseEntity<AdminMovieDTO> getMovieById(@PathVariable Long movieId) {
        try {
            AdminMovieDTO movie = adminService.getMovieById(movieId);
            return ResponseEntity.ok(movie);
        } catch (RuntimeException e) {
            log.error("Movie not found with id: {}", movieId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting movie by id: {}", movieId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/movies")
    @PreAuthorize("hasRole('ADMIN') or hasRole('UPLOADER')")
    public ResponseEntity<AdminMovieDTO> createMovie(
            @RequestParam("title") String title,
            @RequestParam(value = "synopsis", required = false) String synopsis,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "poster", required = false) MultipartFile poster,
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestParam(value = "video", required = false) MultipartFile video,
            @RequestParam(value = "trailer", required = false) MultipartFile trailer,
            @RequestParam(value = "isAvailable", required = false) Boolean isAvailable,
            @RequestParam(value = "actors", required = false) String actors,
            @RequestParam(value = "categories", required = false) String categories,
            @RequestParam(value = "directorName", required = false) String directorName,
            @RequestParam(value = "directors", required = false) String directors,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "language", required = false) String language,
            @RequestParam(value = "ageRating", required = false) String ageRating,
            @RequestParam(value = "imdbRating", required = false) Double imdbRating,
            @RequestParam(value = "isFeatured", required = false) Boolean isFeatured,
            @RequestParam(value = "isTrending", required = false) Boolean isTrending,
            @RequestParam(value = "releaseDate", required = false) String releaseDate,
            @RequestParam(value = "downloadEnabled", required = false) Boolean downloadEnabled,
            @RequestParam(value = "maxDownloadQuality", required = false) String maxDownloadQuality) {
        try {
            String posterUrl = null;
            String thumbnailUrl = null;
            String videoUrl = null;
            String trailerUrl = null;
            
            // Upload poster
            if (poster != null && !poster.isEmpty()) {
                String posterFilename = fileUploadService.uploadImageFile(poster);
                posterUrl = fileUploadService.buildPublicImageUrl(posterFilename);
            }
            
            // Upload thumbnail
            if (thumbnail != null && !thumbnail.isEmpty()) {
                String thumbnailFilename = fileUploadService.uploadImageFile(thumbnail);
                thumbnailUrl = fileUploadService.buildPublicImageUrl(thumbnailFilename);
            }
            
            // Upload video
            if (video != null && !video.isEmpty()) {
                String videoFilename = fileUploadService.uploadVideoFile(video);
                videoUrl = "/api/videos/stream/" + videoFilename;
            }
            
            // Upload trailer
            if (trailer != null && !trailer.isEmpty()) {
                String trailerFilename = fileUploadService.uploadVideoFile(trailer);
                trailerUrl = "/api/videos/stream/" + trailerFilename;
            }
            
            String effectiveDirector = (directorName != null && !directorName.isBlank()) ? directorName : directors;
            MovieManagementRequest request = MovieManagementRequest.builder()
                    .title(title)
                    .synopsis(synopsis)
                    .year(year)
                    .posterUrl(posterUrl)
                    .thumbnailUrl(thumbnailUrl)
                    .videoUrl(videoUrl)
                    .trailerUrl(trailerUrl)
                    .isAvailable(isAvailable)
                    .actors(actors != null && !actors.isEmpty() ? new java.util.ArrayList<>(java.util.Arrays.stream(actors.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList()) : null)
                    .categories(categories != null && !categories.isEmpty() ? new java.util.ArrayList<>(java.util.Arrays.stream(categories.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList()) : null)
                    .directorName(effectiveDirector)
                    .country(country)
                    .language(language)
                    .ageRating(ageRating)
                    .imdbRating(imdbRating)
                    .isFeatured(isFeatured)
                    .isTrending(isTrending)
                    .releaseDate(releaseDate)
                    .downloadEnabled(downloadEnabled)
                    .maxDownloadQuality(maxDownloadQuality)
                    .build();
            
            AdminMovieDTO movie = adminService.createMovie(request);
            
            // Start async FFmpeg processing for multiple resolutions if video was uploaded
            if (video != null && !video.isEmpty() && videoUrl != null) {
                try {
                    // Extract filename from videoUrl (remove /api/videos/stream/ prefix)
                    String videoFilename = videoUrl.replace("/api/videos/stream/", "");
                    Path inputVideoPath = Paths.get("uploads/videos", videoFilename);
                    
                    if (Files.exists(inputVideoPath)) {
                        ffmpegService.processVideoToMultipleResolutions(inputVideoPath, movie.getId(), videoFilename)
                                .thenAccept(result -> {
                                    if (result.isSuccess()) {
                                        log.info("Video processing completed successfully for movie ID: {}", movie.getId());
                                    } else {
                                        log.error("Video processing failed for movie ID: {} - {}", movie.getId(), result.getErrorMessage());
                                    }
                                })
                                .exceptionally(throwable -> {
                                    log.error("Error during video processing for movie ID: {}", movie.getId(), throwable);
                                    return null;
                                });
                        log.info("Started video processing for multiple resolutions for movie ID: {}", movie.getId());
                    } else {
                        log.warn("Video file not found for processing: {}", inputVideoPath);
                    }
                } catch (Exception e) {
                    log.error("Failed to start video processing for movie ID: {}", movie.getId(), e);
                }
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(movie);
        } catch (Exception e) {
            log.error("Error creating movie", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/movies/{movieId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('UPLOADER')")
    public ResponseEntity<AdminMovieDTO> updateMovie(
            @PathVariable Long movieId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "synopsis", required = false) String synopsis,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "poster", required = false) MultipartFile poster,
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestParam(value = "video", required = false) MultipartFile video,
            @RequestParam(value = "trailer", required = false) MultipartFile trailer,
            @RequestParam(value = "isAvailable", required = false) Boolean isAvailable,
            @RequestParam(value = "actors", required = false) String actors,
            @RequestParam(value = "categories", required = false) String categories,
            @RequestParam(value = "directorName", required = false) String directorName,
            @RequestParam(value = "directors", required = false) String directors,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "language", required = false) String language,
            @RequestParam(value = "ageRating", required = false) String ageRating,
            @RequestParam(value = "imdbRating", required = false) Double imdbRating,
            @RequestParam(value = "isFeatured", required = false) Boolean isFeatured,
            @RequestParam(value = "isTrending", required = false) Boolean isTrending,
            @RequestParam(value = "releaseDate", required = false) String releaseDate,
            @RequestParam(value = "downloadEnabled", required = false) Boolean downloadEnabled,
            @RequestParam(value = "maxDownloadQuality", required = false) String maxDownloadQuality) {
        try {
            String posterUrl = null;
            String thumbnailUrl = null;
            String videoUrl = null;
            String trailerUrl = null;
            
            // Upload poster
            if (poster != null && !poster.isEmpty()) {
                String posterFilename = fileUploadService.uploadImageFile(poster);
                posterUrl = fileUploadService.buildPublicImageUrl(posterFilename);
            }
            
            // Upload thumbnail
            if (thumbnail != null && !thumbnail.isEmpty()) {
                String thumbnailFilename = fileUploadService.uploadImageFile(thumbnail);
                thumbnailUrl = fileUploadService.buildPublicImageUrl(thumbnailFilename);
            }
            
            // Upload video
            if (video != null && !video.isEmpty()) {
                String videoFilename = fileUploadService.uploadVideoFile(video);
                videoUrl = "/api/videos/stream/" + videoFilename;
            }
            
            // Upload trailer
            if (trailer != null && !trailer.isEmpty()) {
                String trailerFilename = fileUploadService.uploadVideoFile(trailer);
                trailerUrl = "/api/videos/stream/" + trailerFilename;
            }
            
            String effectiveDirector = (directorName != null && !directorName.isBlank()) ? directorName : directors;
            MovieManagementRequest request = MovieManagementRequest.builder()
                    .title(title)
                    .synopsis(synopsis)
                    .year(year)
                    .posterUrl(posterUrl)
                    .thumbnailUrl(thumbnailUrl)
                    .videoUrl(videoUrl)
                    .trailerUrl(trailerUrl)
                    .isAvailable(isAvailable)
                    .actors(actors != null && !actors.isEmpty() ? new java.util.ArrayList<>(java.util.Arrays.stream(actors.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList()) : null)
                    .categories(categories != null && !categories.isEmpty() ? new java.util.ArrayList<>(java.util.Arrays.stream(categories.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList()) : null)
                    .directorName(effectiveDirector)
                    .country(country)
                    .language(language)
                    .ageRating(ageRating)
                    .imdbRating(imdbRating)
                    .isFeatured(isFeatured)
                    .isTrending(isTrending)
                    .releaseDate(releaseDate)
                    .downloadEnabled(downloadEnabled)
                    .maxDownloadQuality(maxDownloadQuality)
                    .build();
            
            AdminMovieDTO movie = adminService.updateMovie(movieId, request);
            return ResponseEntity.ok(movie);
        } catch (RuntimeException e) {
            log.error("Movie not found with id: {}", movieId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating movie with id: {}", movieId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/movies/{movieId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long movieId) {
        try {
            adminService.deleteMovie(movieId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Movie not found with id: {}", movieId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting movie with id: {}", movieId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/movies/{movieId}/availability")
    public ResponseEntity<AdminMovieDTO> toggleMovieAvailability(@PathVariable Long movieId) {
        try {
            AdminMovieDTO movie = adminService.toggleMovieAvailability(movieId);
            return ResponseEntity.ok(movie);
        } catch (RuntimeException e) {
            log.error("Movie not found with id: {}", movieId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error toggling movie availability for id: {}", movieId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PostMapping("/movies/{movieId}/poster")
    @PreAuthorize("hasRole('ADMIN') or hasRole('UPLOADER')")
    public ResponseEntity<AdminMovieDTO> uploadMoviePoster(
            @PathVariable Long movieId,
            @RequestParam("poster") MultipartFile posterFile) {
        try {
            AdminMovieDTO movie = adminService.getMovieById(movieId);
            return ResponseEntity.ok(movie);
        } catch (RuntimeException e) {
            log.error("Movie not found with id: {}", movieId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error uploading poster for movie id: {}", movieId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/movies/{movieId}/trailer")
    @PreAuthorize("hasRole('ADMIN') or hasRole('UPLOADER')")
    public ResponseEntity<AdminMovieDTO> uploadMovieTrailer(
            @PathVariable Long movieId,
            @RequestParam("trailer") MultipartFile trailerFile) {
        try {
            AdminMovieDTO movie = adminService.getMovieById(movieId);
            return ResponseEntity.ok(movie);
        } catch (RuntimeException e) {
            log.error("Movie not found with id: {}", movieId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error uploading trailer for movie id: {}", movieId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/movies/{movieId}/subtitle")
    @PreAuthorize("hasRole('ADMIN') or hasRole('UPLOADER')")
    public ResponseEntity<AdminMovieDTO> uploadMovieSubtitle(
            @PathVariable Long movieId,
            @RequestParam("subtitle") MultipartFile subtitleFile,
            @RequestParam("language") String language) {
        try {
            AdminMovieDTO movie = adminService.getMovieById(movieId);
            return ResponseEntity.ok(movie);
        } catch (RuntimeException e) {
            log.error("Movie not found with id: {}", movieId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error uploading subtitle for movie id: {}", movieId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/comments")
    public ResponseEntity<Page<AdminCommentDTO>> getAllComments(
            @PageableDefault(size = 20) Pageable pageable) {
        try {
            Page<AdminCommentDTO> comments = adminService.getAllComments(pageable);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            log.error("Error getting all comments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/comments/pending")
    public ResponseEntity<Page<AdminCommentDTO>> getPendingComments(
            @PageableDefault(size = 20) Pageable pageable) {
        try {
            Page<AdminCommentDTO> comments = adminService.getPendingComments(pageable);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            log.error("Error getting pending comments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/comments/movie/{movieId}")
    public ResponseEntity<Page<AdminCommentDTO>> getCommentsByMovie(
            @PathVariable Long movieId,
            @PageableDefault(size = 20) Pageable pageable) {
        try {
            Page<AdminCommentDTO> comments = adminService.getCommentsByMovie(movieId, pageable);
            return ResponseEntity.ok(comments);
        } catch (RuntimeException e) {
            log.error("Movie not found with id: {}", movieId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting comments for movie id: {}", movieId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/comments/{commentId}")
    public ResponseEntity<AdminCommentDTO> getCommentById(@PathVariable Long commentId) {
        try {
            AdminCommentDTO comment = adminService.getCommentById(commentId);
            return ResponseEntity.ok(comment);
        } catch (RuntimeException e) {
            log.error("Comment not found with id: {}", commentId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting comment by id: {}", commentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/comments/{commentId}/moderate")
    public ResponseEntity<AdminCommentDTO> moderateComment(
            @PathVariable Long commentId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String reason) {
        try {
            AdminCommentDTO comment = adminService.moderateComment(commentId, approved, reason);
            return ResponseEntity.ok(comment);
        } catch (RuntimeException e) {
            log.error("Comment not found with id: {}", commentId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error moderating comment with id: {}", commentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        try {
            adminService.deleteComment(commentId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Comment not found with id: {}", commentId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting comment with id: {}", commentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/reports")
    public ResponseEntity<Page<AdminReportDTO>> getAllReports(
            @PageableDefault(size = 20) Pageable pageable) {
        try {
            Page<AdminReportDTO> reports = adminService.getAllReports(pageable);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            log.error("Error getting all reports", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/reports/status/{status}")
    public ResponseEntity<Page<AdminReportDTO>> getReportsByStatus(
            @PathVariable String status,
            @PageableDefault(size = 20) Pageable pageable) {
        try {
            Report.ReportStatus reportStatus = Report.ReportStatus.valueOf(status.toUpperCase());
            Page<AdminReportDTO> reports = adminService.getReportsByStatus(reportStatus, pageable);
            return ResponseEntity.ok(reports);
        } catch (IllegalArgumentException e) {
            log.error("Invalid report status: {}", status, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error getting reports by status: {}", status, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/reports/type/{type}")
    public ResponseEntity<Page<AdminReportDTO>> getReportsByType(
            @PathVariable String type,
            @PageableDefault(size = 20) Pageable pageable) {
        try {
            Report.ReportType reportType = Report.ReportType.valueOf(type.toUpperCase());
            Page<AdminReportDTO> reports = adminService.getReportsByType(reportType, pageable);
            return ResponseEntity.ok(reports);
        } catch (IllegalArgumentException e) {
            log.error("Invalid report type: {}", type, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error getting reports by type: {}", type, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/reports/{reportId}")
    public ResponseEntity<AdminReportDTO> getReportById(@PathVariable Long reportId) {
        try {
            AdminReportDTO report = adminService.getReportById(reportId);
            return ResponseEntity.ok(report);
        } catch (RuntimeException e) {
            log.error("Report not found with id: {}", reportId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting report by id: {}", reportId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/reports/{reportId}/resolve")
    public ResponseEntity<AdminReportDTO> resolveReport(
            @PathVariable Long reportId,
            @RequestParam String status,
            @RequestParam(required = false) String resolutionNote) {
        try {
            AdminReportDTO report = adminService.resolveReport(reportId, status, resolutionNote);
            return ResponseEntity.ok(report);
        } catch (RuntimeException e) {
            log.error("Report not found with id: {}", reportId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error resolving report with id: {}", reportId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/reports")
    public ResponseEntity<Void> createReport(
            @RequestParam Long reportedUserId,
            @RequestParam(required = false) Long reportedCommentId,
            @RequestParam(required = false) Long reportedMovieId,
            @RequestParam String reportType,
            @RequestParam String reason,
            @RequestParam(required = false) String description,
            HttpServletRequest request) {
        try {
            Long reporterId = (Long) request.getAttribute("userId");
            adminService.createReport(reporterId, reportedUserId, reportedCommentId, 
                                    reportedMovieId, reportType, reason, description);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            log.error("Error creating report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PostMapping("/comments/bulk-approve")
    public ResponseEntity<Void> bulkApproveComments(@RequestBody List<Long> commentIds) {
        try {
            adminService.bulkApproveComments(commentIds);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error bulk approving comments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/comments/bulk-delete")
    public ResponseEntity<Void> bulkDeleteComments(@RequestBody List<Long> commentIds) {
        try {
            adminService.bulkDeleteComments(commentIds);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error bulk deleting comments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/reports/bulk-resolve")
    public ResponseEntity<Void> bulkResolveReports(
            @RequestBody List<Long> reportIds,
            @RequestParam String status,
            @RequestParam(required = false) String resolutionNote) {
        try {
            adminService.bulkResolveReports(reportIds, status, resolutionNote);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error bulk resolving reports", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/users/bulk-disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> bulkDisableUsers(
            @RequestBody List<Long> userIds,
            @RequestParam(required = false) String reason) {
        try {
            adminService.bulkDisableUsers(userIds, reason);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error bulk disabling users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
        

    @GetMapping("/top/movies")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminMovieDTO>> getTopMovies(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<AdminMovieDTO> topMovies = adminService.getTopMovies(limit);
            return ResponseEntity.ok(topMovies);
        } catch (Exception e) {
            log.error("Error getting top movies", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/top/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminUserDTO>> getTopUsers(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<AdminUserDTO> topUsers = adminService.getTopUsers(limit);
            return ResponseEntity.ok(topUsers);
        } catch (Exception e) {
            log.error("Error getting top users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== COUNTRY MANAGEMENT ====================

    @PostMapping("/countries")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CountryDTOs.CountryResponse>> createCountry(
            @RequestParam("name") String name,
            @RequestParam(value = "flagUrl", required = false) String flagUrl,
            @RequestParam(value = "flag", required = false) MultipartFile flag,
            @RequestParam(value = "isActive", required = false) Boolean isActive) {
        try {
            if (flag != null && !flag.isEmpty()) {
                String filename = fileUploadService.uploadImageFile(flag);
                flagUrl = fileUploadService.buildPublicImageUrl(filename);
            }
            CountryDTOs.CountryCreateRequest request = CountryDTOs.CountryCreateRequest.builder()
                    .name(name)
                    .flagUrl(flagUrl)
                    .isActive(isActive != null ? isActive : true)
                    .build();
            
            CountryDTOs.CountryResponse response = countryService.createCountry(request);
            ApiResponse<CountryDTOs.CountryResponse> apiResponse = new ApiResponse<>("SUCCESS", "Country created successfully", response);
            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error creating country: {}", e.getMessage());
            ApiResponse<CountryDTOs.CountryResponse> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.badRequest().body(apiResponse);
        } catch (Exception e) {
            log.error("Error creating country", e);
            ApiResponse<CountryDTOs.CountryResponse> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @PutMapping("/countries/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CountryDTOs.CountryResponse>> updateCountry(
            @PathVariable Long id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "flagUrl", required = false) String flagUrl,
            @RequestParam(value = "flag", required = false) MultipartFile flag,
            @RequestParam(value = "isActive", required = false) Boolean isActive) {
        try {
            if (flag != null && !flag.isEmpty()) {
                String filename = fileUploadService.uploadImageFile(flag);
                flagUrl = fileUploadService.buildPublicImageUrl(filename);
            }
            CountryDTOs.CountryUpdateRequest request = CountryDTOs.CountryUpdateRequest.builder()
                    .name(name)
                    .flagUrl(flagUrl)
                    .isActive(isActive)
                    .build();
            
            CountryDTOs.CountryResponse response = countryService.updateCountry(id, request);
            ApiResponse<CountryDTOs.CountryResponse> apiResponse = new ApiResponse<>("SUCCESS", "Country updated successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error updating country: {}", e.getMessage());
            ApiResponse<CountryDTOs.CountryResponse> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.badRequest().body(apiResponse);
        } catch (Exception e) {
            log.error("Error updating country", e);
            ApiResponse<CountryDTOs.CountryResponse> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/countries/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<CountryDTOs.CountryResponse>> getCountryById(@PathVariable Long id) {
        try {
            CountryDTOs.CountryResponse response = countryService.getCountryById(id);
            ApiResponse<CountryDTOs.CountryResponse> apiResponse = new ApiResponse<>("SUCCESS", "Country retrieved successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error getting country: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting country", e);
            ApiResponse<CountryDTOs.CountryResponse> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/countries")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<List<CountryDTOs.CountryResponse>>> getAllCountries() {
        try {
            List<CountryDTOs.CountryResponse> response = countryService.getAllCountries();
            ApiResponse<List<CountryDTOs.CountryResponse>> apiResponse = new ApiResponse<>("SUCCESS", "Countries retrieved successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error getting all countries", e);
            ApiResponse<List<CountryDTOs.CountryResponse>> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/countries/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<List<CountryDTOs.CountrySummaryResponse>>> getActiveCountries() {
        try {
            List<CountryDTOs.CountrySummaryResponse> response = countryService.getAllActiveCountries();
            ApiResponse<List<CountryDTOs.CountrySummaryResponse>> apiResponse = new ApiResponse<>("SUCCESS", "Active countries retrieved successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error getting active countries", e);
            ApiResponse<List<CountryDTOs.CountrySummaryResponse>> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/countries/paginated")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<Page<CountryDTOs.CountryResponse>>> getAllCountriesPaginated(
            @PageableDefault(size = 20) Pageable pageable) {
        try {
            Page<CountryDTOs.CountryResponse> response = countryService.getAllCountriesPaginated(pageable);
            ApiResponse<Page<CountryDTOs.CountryResponse>> apiResponse = new ApiResponse<>("SUCCESS", "Countries retrieved successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error getting paginated countries", e);
            ApiResponse<Page<CountryDTOs.CountryResponse>> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/countries/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<List<CountryDTOs.CountryResponse>>> searchCountriesByName(
            @RequestParam String name) {
        try {
            List<CountryDTOs.CountryResponse> response = countryService.searchCountriesByName(name);
            ApiResponse<List<CountryDTOs.CountryResponse>> apiResponse = new ApiResponse<>("SUCCESS", "Search results retrieved successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error searching countries", e);
            ApiResponse<List<CountryDTOs.CountryResponse>> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/countries/{id}/with-movies")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<CountryDTOs.CountryWithMoviesResponse>> getCountryWithMovies(@PathVariable Long id) {
        try {
            CountryDTOs.CountryWithMoviesResponse response = countryService.getCountryWithMovies(id);
            ApiResponse<CountryDTOs.CountryWithMoviesResponse> apiResponse = new ApiResponse<>("SUCCESS", "Country with movies retrieved successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error getting country with movies: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting country with movies", e);
            ApiResponse<CountryDTOs.CountryWithMoviesResponse> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @DeleteMapping("/countries/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCountry(@PathVariable Long id) {
        try {
            countryService.deleteCountry(id);
            ApiResponse<Void> apiResponse = new ApiResponse<>("SUCCESS", "Country deleted successfully", null);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error deleting country: {}", e.getMessage());
            ApiResponse<Void> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.badRequest().body(apiResponse);
        } catch (Exception e) {
            log.error("Error deleting country", e);
            ApiResponse<Void> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @PutMapping("/countries/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activateCountry(@PathVariable Long id) {
        try {
            countryService.activateCountry(id);
            ApiResponse<Void> apiResponse = new ApiResponse<>("SUCCESS", "Country activated successfully", null);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error activating country: {}", e.getMessage());
            ApiResponse<Void> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.badRequest().body(apiResponse);
        } catch (Exception e) {
            log.error("Error activating country", e);
            ApiResponse<Void> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @PutMapping("/countries/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateCountry(@PathVariable Long id) {
        try {
            countryService.deactivateCountry(id);
            ApiResponse<Void> apiResponse = new ApiResponse<>("SUCCESS", "Country deactivated successfully", null);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error deactivating country: {}", e.getMessage());
            ApiResponse<Void> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.badRequest().body(apiResponse);
        } catch (Exception e) {
            log.error("Error deactivating country", e);
            ApiResponse<Void> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/countries/check-name")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<Boolean>> isCountryNameAvailable(@RequestParam String name) {
        try {
            Boolean response = countryService.isCountryNameAvailable(name);
            ApiResponse<Boolean> apiResponse = new ApiResponse<>("SUCCESS", "Name availability checked", response);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error checking country name availability", e);
            ApiResponse<Boolean> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/countries/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CountryDTOs.CountrySummaryResponse>>> getCountryStatistics() {
        try {
            List<CountryDTOs.CountrySummaryResponse> response = countryService.getCountryStatistics();
            ApiResponse<List<CountryDTOs.CountrySummaryResponse>> apiResponse = new ApiResponse<>("SUCCESS", "Country statistics retrieved successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error getting country statistics", e);
            ApiResponse<List<CountryDTOs.CountrySummaryResponse>> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    // ==================== FORM-BASED COUNTRY OPERATIONS ====================

    @PostMapping("/countries/form")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CountryDTOs.CountryFormResponse>> createCountryFromForm(
            @Valid @RequestBody CountryDTOs.CountryFormCreateRequest request) {
        try {
            CountryDTOs.CountryFormResponse response = countryService.createCountryFromForm(request);
            ApiResponse<CountryDTOs.CountryFormResponse> apiResponse = new ApiResponse<>("SUCCESS", "Country form processed", response);
            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
        } catch (Exception e) {
            log.error("Error processing country form", e);
            ApiResponse<CountryDTOs.CountryFormResponse> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @PutMapping("/countries/{id}/form")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CountryDTOs.CountryFormResponse>> updateCountryFromForm(
            @PathVariable Long id,
            @Valid @RequestBody CountryDTOs.CountryFormUpdateRequest request) {
        try {
            CountryDTOs.CountryFormResponse response = countryService.updateCountryFromForm(id, request);
            ApiResponse<CountryDTOs.CountryFormResponse> apiResponse = new ApiResponse<>("SUCCESS", "Country form processed", response);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error processing country form", e);
            ApiResponse<CountryDTOs.CountryFormResponse> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    // ==================== BULK COUNTRY OPERATIONS ====================

    @PostMapping("/countries/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CountryDTOs.CountryBulkResponse>> createCountriesBulk(
            @Valid @RequestBody CountryDTOs.CountryBulkCreateRequest request) {
        try {
            CountryDTOs.CountryBulkResponse response = countryService.createCountriesBulk(request);
            ApiResponse<CountryDTOs.CountryBulkResponse> apiResponse = new ApiResponse<>("SUCCESS", "Bulk country creation processed", response);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error processing bulk country creation", e);
            ApiResponse<CountryDTOs.CountryBulkResponse> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @DeleteMapping("/countries/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCountriesBulk(@RequestBody List<Long> ids) {
        try {
            countryService.deleteCountriesBulk(ids);
            ApiResponse<Void> apiResponse = new ApiResponse<>("SUCCESS", "Bulk country deletion completed", null);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error processing bulk country deletion", e);
            ApiResponse<Void> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
}
