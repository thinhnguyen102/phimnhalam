package com.aimovie.service;

import com.aimovie.dto.*;
import com.aimovie.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminService {

    Page<AdminUserDTO> getAllUsers(Pageable pageable);
    AdminUserDTO getUserById(Long userId);
    AdminUserDTO updateUserStatus(Long userId, boolean enabled);
    AdminUserDTO updateUserRoles(Long userId, List<String> roles);
    void deleteUser(Long userId);

    Page<AdminMovieDTO> getAllMovies(Pageable pageable);
    AdminMovieDTO getMovieById(Long movieId);
    AdminMovieDTO createMovie(MovieManagementRequest request);
    AdminMovieDTO updateMovie(Long movieId, MovieManagementRequest request);
    void deleteMovie(Long movieId);
    AdminMovieDTO toggleMovieAvailability(Long movieId);

    Page<AdminCommentDTO> getAllComments(Pageable pageable);
    Page<AdminCommentDTO> getPendingComments(Pageable pageable);
    Page<AdminCommentDTO> getCommentsByMovie(Long movieId, Pageable pageable);
    AdminCommentDTO getCommentById(Long commentId);
    AdminCommentDTO moderateComment(Long commentId, boolean approved, String reason);
    void deleteComment(Long commentId);

    Page<AdminReportDTO> getAllReports(Pageable pageable);
    Page<AdminReportDTO> getReportsByStatus(Report.ReportStatus status, Pageable pageable);
    Page<AdminReportDTO> getReportsByType(Report.ReportType reportType, Pageable pageable);
    AdminReportDTO getReportById(Long reportId);
    AdminReportDTO resolveReport(Long reportId, String status, String resolutionNote);
    void createReport(Long reporterId, Long reportedUserId, Long reportedCommentId, 
                     Long reportedMovieId, String reportType, String reason, String description);

    AdminStatsDTO getAdminStats();
    AdminDashboardDTO getAdminDashboard();
    List<MonthlyStatsDTO> getMonthlyStats(int year);
    List<AdminMovieDTO> getTopMovies(int limit);
    List<AdminUserDTO> getTopUsers(int limit);

    AdminMovieDTO uploadMovieTrailer(Long movieId, String trailerUrl);
    AdminMovieDTO uploadMoviePoster(Long movieId, String posterUrl);
    AdminMovieDTO uploadMovieSubtitle(Long movieId, String subtitleUrl, String language);

    void bulkApproveComments(List<Long> commentIds);
    void bulkDeleteComments(List<Long> commentIds);
    void bulkResolveReports(List<Long> reportIds, String status, String resolutionNote);
    void bulkDisableUsers(List<Long> userIds, String reason);
}
