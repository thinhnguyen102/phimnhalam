package com.aimovie.repository;

import com.aimovie.entity.Report;
import com.aimovie.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    Page<Report> findByStatusOrderByCreatedAtDesc(Report.ReportStatus status, Pageable pageable);

    Page<Report> findByReportTypeOrderByCreatedAtDesc(Report.ReportType reportType, Pageable pageable);

    Page<Report> findByStatusInOrderByCreatedAtDesc(List<Report.ReportStatus> statuses, Pageable pageable);

    long countByStatus(Report.ReportStatus status);

    long countByReportType(Report.ReportType reportType);

    Page<Report> findByReporterIdOrderByCreatedAtDesc(Long reporterId, Pageable pageable);

    Page<Report> findByReportedUserIdOrderByCreatedAtDesc(Long reportedUserId, Pageable pageable);

    Page<Report> findByReportedCommentIdOrderByCreatedAtDesc(Long commentId, Pageable pageable);

    Page<Report> findByReportedMovieIdOrderByCreatedAtDesc(Long movieId, Pageable pageable);

    @Query("SELECT COUNT(r) FROM Report r WHERE YEAR(r.createdAt) = :year AND MONTH(r.createdAt) = :month")
    long countByYearAndMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.reportType = :reportType AND YEAR(r.createdAt) = :year AND MONTH(r.createdAt) = :month")
    long countByReportTypeAndYearAndMonth(@Param("reportType") Report.ReportType reportType, 
                                         @Param("year") int year, 
                                         @Param("month") int month);

    void deleteByReporter(User user);

    long countByReporterId(Long reporterId);
}
