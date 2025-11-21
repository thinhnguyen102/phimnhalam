package com.aimovie.serviceImpl;

import com.aimovie.dto.*;
import com.aimovie.entity.*;
import com.aimovie.repository.*;
import com.aimovie.service.AdminService;
import com.aimovie.service.FileUploadService;
import com.aimovie.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final ActorRepository actorRepository;
    private final CategoryRepository categoryRepository;
    private final CountryRepository countryRepository;
    private final DirectorRepository directorRepository;
    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;
    private final FavoriteRepository favoriteRepository;
    private final WatchHistoryRepository watchHistoryRepository;
    private final WatchlistRepository watchlistRepository;
    private final WatchlistCollectionRepository watchlistCollectionRepository;
    private final RatingRepository ratingRepository;
    private final MovieService movieService;
    private final SubtitleRepository subtitleRepository;
    private final FileUploadService fileUploadService;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToAdminUserDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return convertToAdminUserDTO(user);
    }

    @Override
    public AdminUserDTO updateUserStatus(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setEnabled(enabled);
        user = userRepository.save(user);
        return convertToAdminUserDTO(user);
    }

    @Override
    public AdminUserDTO updateUserRoles(Long userId, List<String> roles) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Set<Role> roleSet = roles.stream()
                .map(Role::valueOf)
                .collect(Collectors.toSet());
        user.setRoles(roleSet);
        user = userRepository.save(user);
        return convertToAdminUserDTO(user);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Delete all user-related data before deleting the user
        // This prevents foreign key constraint violations
        
        // Delete favorites
        favoriteRepository.deleteByUser(user);
        
        // Delete watch history
        watchHistoryRepository.deleteByUser(user);
        
        // Delete watchlist items
        watchlistRepository.deleteByUserId(userId);
        
        // Delete watchlist collections
        watchlistCollectionRepository.deleteByUser(user);
        
        // Delete ratings
        ratingRepository.deleteByUser(user);
        
        // Delete comments
        commentRepository.deleteByUser(user);
        
        // Delete reports created by user
        reportRepository.deleteByReporter(user);
        
        // Finally, delete the user
        userRepository.delete(user);
        
        log.info("Successfully deleted user with id: {} and all related data", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminMovieDTO> getAllMovies(Pageable pageable) {
        return movieRepository.findAll(pageable)
                .map(this::convertToAdminMovieDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminMovieDTO getMovieById(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));
        return convertToAdminMovieDTO(movie);
    }

    @Override
    public AdminMovieDTO createMovie(MovieManagementRequest request) {
        validateActorsExist(request.getActors());
        if (request.getCategories() != null && !request.getCategories().isEmpty()) {
            validateCategoriesExist(request.getCategories());
        }
        if (request.getCountry() != null && !request.getCountry().trim().isEmpty()) {
            validateCountryExists(request.getCountry());
        }
        Movie movie = Movie.builder()
                .title(request.getTitle())
                .synopsis(request.getSynopsis())
                .year(request.getYear())
                .actors(request.getActors() != null ? request.getActors() : java.util.List.of())
                .posterUrl(request.getPosterUrl())
                .thumbnailUrl(request.getThumbnailUrl())
                .videoUrl(request.getVideoUrl())
                .videoQuality(request.getVideoQuality())
                .videoDuration(request.getVideoDuration())
                .language(request.getLanguage())
                .ageRating(request.getAgeRating())
                .imdbRating(request.getImdbRating())
                .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
                .isTrending(request.getIsTrending() != null ? request.getIsTrending() : false)
                .trailerUrl(request.getTrailerUrl())
                .downloadEnabled(request.getDownloadEnabled() != null ? request.getDownloadEnabled() : false)
                .maxDownloadQuality(request.getMaxDownloadQuality())
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .build();

        if (request.getReleaseDate() != null && !request.getReleaseDate().isBlank()) {
            try {
                movie.setReleaseDate(java.time.LocalDate.parse(request.getReleaseDate()));
            } catch (Exception ignored) {
            }
        }
        
        if (request.getDirectorName() != null && !request.getDirectorName().trim().isEmpty()) {
            Director director = directorRepository.findByNameIgnoreCase(request.getDirectorName().trim())
                    .orElseGet(() -> {
                        Director newDirector = Director.builder()
                                .name(request.getDirectorName().trim())
                                .isActive(true)
                                .build();
                        return directorRepository.save(newDirector);
                    });
            movie.setDirector(director);
        } else {
            Director defaultDirector = directorRepository.findByNameIgnoreCase("Unknown Director")
                    .orElseGet(() -> {
                        Director newDirector = Director.builder()
                                .name("Unknown Director")
                                .isActive(true)
                                .build();
                        return directorRepository.save(newDirector);
                    });
            movie.setDirector(defaultDirector);
        }
        
        if (request.getCountry() != null && !request.getCountry().trim().isEmpty()) {
            Country country = countryRepository.findByNameIgnoreCase(request.getCountry().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Country not found: " + request.getCountry()));
            movie.setCountry(country);
        }
        if (request.getCategories() != null && !request.getCategories().isEmpty()) {
            var cats = categoryRepository.findByNameLowerIn(request.getCategories().stream().map(String::trim).map(String::toLowerCase).toList());
            movie.setCategories(new java.util.HashSet<>(cats));
        }
        
        movie = movieRepository.save(movie);
        return convertToAdminMovieDTO(movie);
    }

    @Override
    public AdminMovieDTO updateMovie(Long movieId, MovieManagementRequest request) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));
        
        if (request.getActors() != null) {
            validateActorsExist(request.getActors());
            movie.setActors(request.getActors());
        }
        if (request.getCategories() != null) {
            validateCategoriesExist(request.getCategories());
            var cats = categoryRepository.findByNameLowerIn(request.getCategories().stream().map(String::trim).map(String::toLowerCase).toList());
            movie.setCategories(new java.util.HashSet<>(cats));
        }
        if (request.getDirectorName() != null && !request.getDirectorName().trim().isEmpty()) {
            Director director = directorRepository.findByNameIgnoreCase(request.getDirectorName().trim())
                    .orElseGet(() -> {
                        Director newDirector = Director.builder()
                                .name(request.getDirectorName().trim())
                                .isActive(true)
                                .build();
                        return directorRepository.save(newDirector);
                    });
            movie.setDirector(director);
        }
        if (request.getCountry() != null && !request.getCountry().trim().isEmpty()) {
            validateCountryExists(request.getCountry());
            Country country = countryRepository.findByNameIgnoreCase(request.getCountry().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Country not found: " + request.getCountry()));
            movie.setCountry(country);
        }
        if (request.getTitle() != null) movie.setTitle(request.getTitle());
        if (request.getSynopsis() != null) movie.setSynopsis(request.getSynopsis());
        if (request.getYear() != null) movie.setYear(request.getYear());
        if (request.getPosterUrl() != null) movie.setPosterUrl(request.getPosterUrl());
        if (request.getThumbnailUrl() != null) movie.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getVideoUrl() != null) movie.setVideoUrl(request.getVideoUrl());
        if (request.getVideoQuality() != null) movie.setVideoQuality(request.getVideoQuality());
        if (request.getVideoDuration() != null) movie.setVideoDuration(request.getVideoDuration());
        if (request.getLanguage() != null) movie.setLanguage(request.getLanguage());
        if (request.getAgeRating() != null) movie.setAgeRating(request.getAgeRating());
        if (request.getImdbRating() != null) movie.setImdbRating(request.getImdbRating());
        if (request.getIsFeatured() != null) movie.setIsFeatured(request.getIsFeatured());
        if (request.getIsTrending() != null) movie.setIsTrending(request.getIsTrending());
        if (request.getTrailerUrl() != null) movie.setTrailerUrl(request.getTrailerUrl());
        if (request.getReleaseDate() != null && !request.getReleaseDate().isBlank()) {
            try {
                movie.setReleaseDate(java.time.LocalDate.parse(request.getReleaseDate()));
            } catch (Exception ignored) {}
        }
        if (request.getDownloadEnabled() != null) movie.setDownloadEnabled(request.getDownloadEnabled());
        if (request.getMaxDownloadQuality() != null) movie.setMaxDownloadQuality(request.getMaxDownloadQuality());
        if (request.getIsAvailable() != null) {
            movie.setIsAvailable(request.getIsAvailable());
        }
        
        movie = movieRepository.save(movie);
        return convertToAdminMovieDTO(movie);
    }

    @Override
    public void deleteMovie(Long movieId) {
        movieService.deleteMovie(movieId);
    }

    @Override
    public AdminMovieDTO toggleMovieAvailability(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));
        movie.setIsAvailable(!movie.getIsAvailable());
        movie = movieRepository.save(movie);
        return convertToAdminMovieDTO(movie);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminCommentDTO> getAllComments(Pageable pageable) {
        return commentRepository.findAll(pageable)
                .map(this::convertToAdminCommentDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminCommentDTO> getPendingComments(Pageable pageable) {
        return commentRepository.findByIsApprovedFalseAndIsDeletedFalseOrderByCreatedAtDesc(pageable)
                .map(this::convertToAdminCommentDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminCommentDTO> getCommentsByMovie(Long movieId, Pageable pageable) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));
        return commentRepository.findByMovieAndIsDeletedFalseOrderByCreatedAtDesc(movie, pageable)
                .map(this::convertToAdminCommentDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminCommentDTO getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));
        return convertToAdminCommentDTO(comment);
    }

    @Override
    public AdminCommentDTO moderateComment(Long commentId, boolean approved, String reason) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));
        
        comment.setIsApproved(approved);
        if (!approved) {
            comment.setIsDeleted(true);
        }
        
        comment = commentRepository.save(comment);
        return convertToAdminCommentDTO(comment);
    }

    @Override
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));
        comment.setIsDeleted(true);
        commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminReportDTO> getAllReports(Pageable pageable) {
        return reportRepository.findAll(pageable)
                .map(this::convertToAdminReportDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminReportDTO> getReportsByStatus(Report.ReportStatus status, Pageable pageable) {
        return reportRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                .map(this::convertToAdminReportDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminReportDTO> getReportsByType(Report.ReportType reportType, Pageable pageable) {
        return reportRepository.findByReportTypeOrderByCreatedAtDesc(reportType, pageable)
                .map(this::convertToAdminReportDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminReportDTO getReportById(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + reportId));
        return convertToAdminReportDTO(report);
    }

    @Override
    public AdminReportDTO resolveReport(Long reportId, String status, String resolutionNote) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + reportId));
        
        report.setStatus(Report.ReportStatus.valueOf(status));
        report.setResolutionNote(resolutionNote);
        report.setUpdatedAt(LocalDateTime.now());
        
        report = reportRepository.save(report);
        return convertToAdminReportDTO(report);
    }

    @Override
    public void createReport(Long reporterId, Long reportedUserId, Long reportedCommentId, 
                           Long reportedMovieId, String reportType, String reason, String description) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new RuntimeException("Reporter not found with id: " + reporterId));
        
        Report report = Report.builder()
                .reporter(reporter)
                .reason(reason)
                .description(description)
                .reportType(Report.ReportType.valueOf(reportType))
                .status(Report.ReportStatus.PENDING)
                .build();
        
        if (reportedUserId != null) {
            User reportedUser = userRepository.findById(reportedUserId)
                    .orElseThrow(() -> new RuntimeException("Reported user not found with id: " + reportedUserId));
            report.setReportedUser(reportedUser);
        }
        
        if (reportedCommentId != null) {
            Comment reportedComment = commentRepository.findById(reportedCommentId)
                    .orElseThrow(() -> new RuntimeException("Reported comment not found with id: " + reportedCommentId));
            report.setReportedComment(reportedComment);
        }
        
        if (reportedMovieId != null) {
            Movie reportedMovie = movieRepository.findById(reportedMovieId)
                    .orElseThrow(() -> new RuntimeException("Reported movie not found with id: " + reportedMovieId));
            report.setReportedMovie(reportedMovie);
        }
        
        reportRepository.save(report);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStatsDTO getAdminStats() {
        long totalUsers = userRepository.count();
        long totalMovies = movieRepository.count();
        long totalComments = ratingRepository.countByCommentIsNotNull();
        long totalReports = reportRepository.count();
        long pendingReports = reportRepository.countByStatus(Report.ReportStatus.PENDING);
        long pendingComments = commentRepository.countByIsApprovedFalseAndIsDeletedFalse();
        long activeUsers = userRepository.countByEnabledTrue();
        long disabledUsers = userRepository.countByEnabledFalse();
        
        // Calculate total views from all movies
        long totalViews = movieRepository.findAll().stream()
                .mapToLong(movie -> movie.getViewCount() != null ? movie.getViewCount() : 0L)
                .sum();
        
        // Calculate average rating from all movies
        List<Movie> allMovies = movieRepository.findAll();
        double averageRating = allMovies.stream()
                .filter(movie -> movie.getAverageRating() != null && movie.getAverageRating() > 0)
                .mapToDouble(Movie::getAverageRating)
                .average()
                .orElse(0.0);
        
        // Get current month stats
        int currentYear = java.time.LocalDate.now().getYear();
        int currentMonth = java.time.LocalDate.now().getMonthValue();
        MonthlyStatsDTO currentMonthStats = getMonthlyStatsForMonth(currentYear, currentMonth);
        
        return AdminStatsDTO.builder()
                .totalUsers(totalUsers)
                .totalMovies(totalMovies)
                .totalComments(totalComments)
                .totalReports(totalReports)
                .pendingReports(pendingReports)
                .pendingComments(pendingComments)
                .activeUsers(activeUsers)
                .disabledUsers(disabledUsers)
                .totalViews(totalViews)
                .averageRating(Math.round(averageRating * 10.0) / 10.0) // Round to 1 decimal
                .monthlyStats(currentMonthStats)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardDTO getAdminDashboard() {
        AdminStatsDTO stats = getAdminStats();
        
        List<AdminReportDTO> recentReports = getReportsByStatus(Report.ReportStatus.PENDING, Pageable.ofSize(5))
                .getContent();
        
        List<AdminCommentDTO> pendingComments = getPendingComments(Pageable.ofSize(5))
                .getContent();
        
        // Get recent users (sorted by creation date)
        List<AdminUserDTO> recentUsers = userRepository.findAll(
                org.springframework.data.domain.PageRequest.of(0, 5, 
                org.springframework.data.domain.Sort.by("createdAt").descending()))
                .getContent()
                .stream()
                .map(this::convertToAdminUserDTO)
                .collect(java.util.stream.Collectors.toList());
        
        // Get top movies (sorted by view count)
        List<AdminMovieDTO> topMovies = movieRepository.findAll(
                org.springframework.data.domain.PageRequest.of(0, 5, 
                org.springframework.data.domain.Sort.by("viewCount").descending()))
                .getContent()
                .stream()
                .map(this::convertToAdminMovieDTO)
                .collect(java.util.stream.Collectors.toList());
        
        return AdminDashboardDTO.builder()
                .stats(stats)
                .recentReports(recentReports)
                .pendingComments(pendingComments)
                .recentUsers(recentUsers)
                .topMovies(topMovies)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyStatsDTO> getMonthlyStats(int year) {
        List<MonthlyStatsDTO> monthlyStats = new java.util.ArrayList<>();
        
        for (int month = 1; month <= 12; month++) {
            MonthlyStatsDTO stats = getMonthlyStatsForMonth(year, month);
            monthlyStats.add(stats);
        }
        
        return monthlyStats;
    }
    
    private MonthlyStatsDTO getMonthlyStatsForMonth(int year, int month) {
        // Count new users created in this month
        long newUsers = userRepository.findAll().stream()
                .filter(user -> user.getCreatedAt() != null)
                .filter(user -> user.getCreatedAt().getYear() == year && 
                               user.getCreatedAt().getMonthValue() == month)
                .count();
        
        // Count new movies created in this month
        long newMovies = movieRepository.findAll().stream()
                .filter(movie -> movie.getCreatedAt() != null)
                .filter(movie -> movie.getCreatedAt().getYear() == year && 
                                movie.getCreatedAt().getMonthValue() == month)
                .count();
        
        // Count new comments (ratings with comments) created in this month
        long newComments = ratingRepository.countByCommentIsNotNullAndYearAndMonth(year, month);
        
        // Count new reports created in this month
        long newReports = reportRepository.findAll().stream()
                .filter(report -> report.getCreatedAt() != null)
                .filter(report -> report.getCreatedAt().getYear() == year && 
                                 report.getCreatedAt().getMonthValue() == month)
                .count();
        
        // Calculate total views for movies created in this month
        long totalViews = movieRepository.findAll().stream()
                .filter(movie -> movie.getCreatedAt() != null)
                .filter(movie -> movie.getCreatedAt().getYear() == year && 
                                movie.getCreatedAt().getMonthValue() == month)
                .mapToLong(movie -> movie.getViewCount() != null ? movie.getViewCount() : 0L)
                .sum();
        
        return MonthlyStatsDTO.builder()
                .year(year)
                .month(month)
                .newUsers(newUsers)
                .newMovies(newMovies)
                .newComments(newComments)
                .newReports(newReports)
                .totalViews(totalViews)
                .revenue(0.0) // Placeholder for revenue calculation
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminMovieDTO> getTopMovies(int limit) {
        return movieRepository.findAll(Pageable.ofSize(limit))
                .getContent()
                .stream()
                .map(this::convertToAdminMovieDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserDTO> getTopUsers(int limit) {
        return userRepository.findAll(Pageable.ofSize(limit))
                .getContent()
                .stream()
                .map(this::convertToAdminUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AdminMovieDTO uploadMovieTrailer(Long movieId, String trailerUrl) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));
        return convertToAdminMovieDTO(movie);
    }

    @Override
    public AdminMovieDTO uploadMoviePoster(Long movieId, String posterUrl) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));
        movie.setPosterUrl(posterUrl);
        movie = movieRepository.save(movie);
        return convertToAdminMovieDTO(movie);
    }

    @Override
    public AdminMovieDTO uploadMovieSubtitle(Long movieId, MultipartFile subtitleFile, String languageCode, String languageName, Boolean isDefault) {
        if (subtitleFile == null || subtitleFile.isEmpty()) {
            throw new IllegalArgumentException("Subtitle file is required");
        }
        if (languageCode == null || languageCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Language code is required");
        }
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));

        String normalizedLanguageCode = languageCode.trim().toLowerCase();
        String normalizedLanguageName = (languageName != null && !languageName.trim().isEmpty())
                ? languageName.trim()
                : normalizedLanguageCode.toUpperCase();

        String storedFilename;
        try {
            storedFilename = fileUploadService.uploadSubtitleFile(subtitleFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload subtitle file: " + e.getMessage(), e);
        }
        String subtitleUrl = fileUploadService.buildPublicSubtitleUrl(storedFilename);

        if (Boolean.TRUE.equals(isDefault)) {
            subtitleRepository.findByMovieAndIsDefaultTrueAndIsAvailableTrue(movie)
                    .ifPresent(existing -> {
                        existing.setIsDefault(false);
                        subtitleRepository.save(existing);
                    });
        }

        Subtitle subtitle = subtitleRepository
                .findByMovieAndLanguageCodeAndIsAvailableTrue(movie, normalizedLanguageCode)
                .orElse(Subtitle.builder()
                        .movie(movie)
                        .languageCode(normalizedLanguageCode)
                        .build());

        subtitle.setLanguageName(normalizedLanguageName);
        subtitle.setSubtitleUrl(subtitleUrl);
        subtitle.setFileSizeBytes(subtitleFile.getSize());
        subtitle.setEncoding("UTF-8");
        subtitle.setIsAutoGenerated(false);
        subtitle.setIsDefault(Boolean.TRUE.equals(isDefault));
        subtitle.setIsAvailable(true);

        subtitleRepository.save(subtitle);

        return convertToAdminMovieDTO(movie);
    }

    @Override
    public void bulkApproveComments(List<Long> commentIds) {
        commentIds.forEach(commentId -> {
            Comment comment = commentRepository.findById(commentId).orElse(null);
            if (comment != null) {
                comment.setIsApproved(true);
                commentRepository.save(comment);
            }
        });
    }

    @Override
    public void bulkDeleteComments(List<Long> commentIds) {
        commentIds.forEach(commentId -> {
            Comment comment = commentRepository.findById(commentId).orElse(null);
            if (comment != null) {
                comment.setIsDeleted(true);
                commentRepository.save(comment);
            }
        });
    }

    @Override
    public void bulkResolveReports(List<Long> reportIds, String status, String resolutionNote) {
        reportIds.forEach(reportId -> {
            Report report = reportRepository.findById(reportId).orElse(null);
            if (report != null) {
                report.setStatus(Report.ReportStatus.valueOf(status));
                report.setResolutionNote(resolutionNote);
                reportRepository.save(report);
            }
        });
    }

    @Override
    public void bulkDisableUsers(List<Long> userIds, String reason) {
        userIds.forEach(userId -> {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                user.setEnabled(false);
                userRepository.save(user);
            }
        });
    }

    private AdminUserDTO convertToAdminUserDTO(User user) {
        long totalComments = ratingRepository.countByUserIdAndCommentIsNotNull(user.getId());
        long totalReports = reportRepository.countByReporterId(user.getId());
        long totalMovies = 0L; // No ownership tracking; keep zero or derive if model supports it
        return AdminUserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .roles(user.getRoles().stream().map(Enum::name).collect(Collectors.toList()))
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .totalComments(totalComments)
                .totalReports(totalReports)
                .totalMovies(totalMovies)
                .build();
    }

    private AdminMovieDTO convertToAdminMovieDTO(Movie movie) {
        return AdminMovieDTO.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .synopsis(movie.getSynopsis())
                .year(movie.getYear())
                .categories(movie.getCategories().stream().map(Category::getName).toList())
                .director(movie.getDirector() != null ? DirectorDTO.builder()
                        .id(movie.getDirector().getId())
                        .name(movie.getDirector().getName())
                        .biography(movie.getDirector().getBiography())
                        .birthDate(movie.getDirector().getBirthDate())
                        .nationality(movie.getDirector().getNationality())
                        .photoUrl(movie.getDirector().getPhotoUrl())
                        .isActive(movie.getDirector().getIsActive())
                        .createdAt(movie.getDirector().getCreatedAt())
                        .updatedAt(movie.getDirector().getUpdatedAt())
                        .build() : null)
                .actors(movie.getActors())
                .posterUrl(movie.getPosterUrl())
                .thumbnailUrl(movie.getThumbnailUrl())
                .videoUrl(movie.getVideoUrl())
                .videoQuality(movie.getVideoQuality())
                .videoDuration(movie.getVideoDuration())
                .fileSizeBytes(movie.getFileSizeBytes())
                .language(movie.getLanguage())
                .ageRating(movie.getAgeRating())
                .imdbRating(movie.getImdbRating())
                .isFeatured(movie.getIsFeatured())
                .isTrending(movie.getIsTrending())
                .releaseDate(movie.getReleaseDate())
                .trailerUrl(movie.getTrailerUrl())
                .availableQualities(movie.getAvailableQualities())
                .countryName(movie.getCountry() != null ? movie.getCountry().getName() : null)
                .isAvailable(movie.getIsAvailable())
                .createdAt(movie.getCreatedAt())
                .updatedAt(movie.getUpdatedAt())
                .viewCount(movie.getViewCount() != null ? movie.getViewCount() : 0L)
                .commentCount(movie.getCommentCount() != null ? movie.getCommentCount() : 0L)
                .averageRating(movie.getAverageRating() != null ? movie.getAverageRating() : 0.0)
                .build();
    }

    private void validateCategoriesExist(List<String> categoryNames) {
        if (categoryNames == null || categoryNames.isEmpty()) {
            return;
        }
        var namesLower = categoryNames.stream()
                .filter(n -> n != null && !n.isBlank())
                .map(String::trim)
                .map(String::toLowerCase)
                .toList();
        if (namesLower.isEmpty()) {
            return;
        }
        var existing = new java.util.HashSet<>(categoryRepository.findExistingLowerNames(namesLower));
        var missing = namesLower.stream()
                .filter(n -> !existing.contains(n))
                .distinct()
                .toList();
        if (!missing.isEmpty()) {
            // Auto-create missing categories
            for (String missingName : missing) {
                Category newCategory = Category.builder()
                        .name(missingName)
                        .description("Auto-created category")
                        .build();
                categoryRepository.save(newCategory);
                log.info("Auto-created category: {}", missingName);
            }
        }
    }

    private void validateActorsExist(List<String> actorNames) {
        if (actorNames == null || actorNames.isEmpty()) {
            return;
        }
        var namesLower = actorNames.stream()
                .filter(n -> n != null && !n.isBlank())
                .map(String::trim)
                .map(String::toLowerCase)
                .toList();
        if (namesLower.isEmpty()) {
            return;
        }
        var existing = new java.util.HashSet<>(actorRepository.findExistingLowerNames(namesLower));
        var missing = namesLower.stream()
                .filter(n -> !existing.contains(n))
                .distinct()
                .toList();
        if (!missing.isEmpty()) {
            // Auto-create missing actors
            for (String missingName : missing) {
                Actor newActor = Actor.builder()
                        .name(missingName)
                        .description("Auto-created actor")
                        .build();
                actorRepository.save(newActor);
                log.info("Auto-created actor: {}", missingName);
            }
        }
    }

    private void validateCountryExists(String countryName) {
        if (countryName == null || countryName.trim().isEmpty()) {
            return;
        }
        String trimmedName = countryName.trim();
        if (!countryRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new IllegalArgumentException("Country not found: " + trimmedName);
        }
    }

    private AdminCommentDTO convertToAdminCommentDTO(Comment comment) {
        return AdminCommentDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .username(comment.getUser().getUsername())
                .movieTitle(comment.getMovie().getTitle())
                .movieId(comment.getMovie().getId())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .isApproved(comment.getIsApproved())
                .isDeleted(comment.getIsDeleted())
                .likeCount(comment.getLikeCount())
                .dislikeCount(comment.getDislikeCount())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    private AdminReportDTO convertToAdminReportDTO(Report report) {
        return AdminReportDTO.builder()
                .id(report.getId())
                .reason(report.getReason())
                .description(report.getDescription())
                .reporterUsername(report.getReporter().getUsername())
                .reportedUserUsername(report.getReportedUser() != null ? report.getReportedUser().getUsername() : null)
                .reportedCommentContent(report.getReportedComment() != null ? report.getReportedComment().getContent() : null)
                .reportedMovieTitle(report.getReportedMovie() != null ? report.getReportedMovie().getTitle() : null)
                .reportType(report.getReportType().name())
                .status(report.getStatus().name())
                .resolvedByUsername(report.getResolvedBy() != null ? report.getResolvedBy().getUsername() : null)
                .resolutionNote(report.getResolutionNote())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}
