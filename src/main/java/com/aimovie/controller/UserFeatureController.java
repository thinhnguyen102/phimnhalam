package com.aimovie.controller;

import com.aimovie.dto.*;
import com.aimovie.service.UserFeatureService;
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

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MODERATOR') or hasRole('UPLOADER')")
public class UserFeatureController {

    private final UserFeatureService userFeatureService;

    // ==================== USER PROFILE MANAGEMENT ====================

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            UserProfileDTO profile = userFeatureService.getUserProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Error getting user profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDTO> updateUserProfile(
            @Valid @RequestBody UserProfileUpdateRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            UserProfileDTO profile = userFeatureService.updateUserProfile(userId, request);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Error updating user profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody PasswordChangeRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            userFeatureService.changePassword(userId, request);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("Error changing password: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error changing password", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/avatar")
    public ResponseEntity<Void> updateAvatar(
            @RequestParam("avatar") MultipartFile avatarFile,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            // Implementation for file upload would go here
            // For now, we'll just return success
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error updating avatar", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<UserStatsDTO> getUserStats(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            UserStatsDTO stats = userFeatureService.getUserStats(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting user stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== SOCIAL LOGIN ====================

    @PostMapping("/social-login")
    public ResponseEntity<UserProfileDTO> socialLogin(@Valid @RequestBody SocialLoginRequest request) {
        try {
            UserProfileDTO profile = userFeatureService.socialLogin(request);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Error with social login", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== WATCH HISTORY ====================

    @GetMapping("/watch-history")
    public ResponseEntity<Page<WatchHistoryDTO>> getWatchHistory(
            @PageableDefault(size = 20) Pageable pageable,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Page<WatchHistoryDTO> history = userFeatureService.getWatchHistory(userId, pageable);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error getting watch history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/watch-history/recent")
    public ResponseEntity<List<WatchHistoryDTO>> getRecentWatchHistory(
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<WatchHistoryDTO> history = userFeatureService.getRecentWatchHistory(userId, limit);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error getting recent watch history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/watch-history/incomplete")
    public ResponseEntity<List<WatchHistoryDTO>> getIncompleteWatchHistory(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<WatchHistoryDTO> history = userFeatureService.getIncompleteWatchHistory(userId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error getting incomplete watch history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/watch-history")
    public ResponseEntity<WatchHistoryDTO> updateWatchHistory(
            @Valid @RequestBody WatchHistoryUpdateRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            WatchHistoryDTO history = userFeatureService.updateWatchHistory(userId, request);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error updating watch history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/watch-history/{movieId}")
    public ResponseEntity<Void> deleteWatchHistory(
            @PathVariable Long movieId,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            userFeatureService.deleteWatchHistory(userId, movieId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting watch history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/watch-history")
    public ResponseEntity<Void> clearWatchHistory(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            userFeatureService.clearWatchHistory(userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error clearing watch history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== FAVORITES ====================

    @GetMapping("/favorites")
    public ResponseEntity<Page<FavoriteDTO>> getFavorites(
            @PageableDefault(size = 20) Pageable pageable,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Page<FavoriteDTO> favorites = userFeatureService.getFavorites(userId, pageable);
            return ResponseEntity.ok(favorites);
        } catch (Exception e) {
            log.error("Error getting favorites", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/favorites/{movieId}")
    public ResponseEntity<FavoriteDTO> addToFavorites(
            @PathVariable Long movieId,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            FavoriteDTO favorite = userFeatureService.addToFavorites(userId, movieId);
            return ResponseEntity.ok(favorite);
        } catch (Exception e) {
            log.error("Error adding to favorites", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/favorites/{movieId}")
    public ResponseEntity<Void> removeFromFavorites(
            @PathVariable Long movieId,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            userFeatureService.removeFromFavorites(userId, movieId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error removing from favorites", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/favorites/{movieId}/check")
    public ResponseEntity<Boolean> isFavorite(
            @PathVariable Long movieId,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            boolean isFavorite = userFeatureService.isFavorite(userId, movieId);
            return ResponseEntity.ok(isFavorite);
        } catch (Exception e) {
            log.error("Error checking favorite status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== WATCHLIST ====================

    @GetMapping("/watchlist")
    public ResponseEntity<Page<WatchlistDTO>> getWatchlist(
            @PageableDefault(size = 20) Pageable pageable,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Page<WatchlistDTO> watchlist = userFeatureService.getWatchlist(userId, pageable);
            return ResponseEntity.ok(watchlist);
        } catch (Exception e) {
            log.error("Error getting watchlist", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/watchlist/{movieId}")
    public ResponseEntity<WatchlistDTO> addToWatchlist(
            @PathVariable Long movieId,
            @RequestParam(required = false) String notes,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            WatchlistDTO watchlist = userFeatureService.addToWatchlist(userId, movieId, notes);
            return ResponseEntity.ok(watchlist);
        } catch (Exception e) {
            log.error("Error adding to watchlist", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/watchlist/{movieId}")
    public ResponseEntity<Void> removeFromWatchlist(
            @PathVariable Long movieId,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            userFeatureService.removeFromWatchlist(userId, movieId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error removing from watchlist", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/watchlist/{movieId}")
    public ResponseEntity<WatchlistDTO> updateWatchlistItem(
            @PathVariable Long movieId,
            @Valid @RequestBody WatchlistUpdateRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            WatchlistDTO watchlist = userFeatureService.updateWatchlistItem(userId, movieId, request);
            return ResponseEntity.ok(watchlist);
        } catch (Exception e) {
            log.error("Error updating watchlist item", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/watchlist/{movieId}/check")
    public ResponseEntity<Boolean> isInWatchlist(
            @PathVariable Long movieId,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            boolean isInWatchlist = userFeatureService.isInWatchlist(userId, movieId);
            return ResponseEntity.ok(isInWatchlist);
        } catch (Exception e) {
            log.error("Error checking watchlist status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/watchlist/priority")
    public ResponseEntity<List<WatchlistDTO>> getWatchlistByPriority(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<WatchlistDTO> watchlist = userFeatureService.getWatchlistByPriority(userId);
            return ResponseEntity.ok(watchlist);
        } catch (Exception e) {
            log.error("Error getting watchlist by priority", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== STREAMING PREFERENCES ====================

    @PutMapping("/preferences/streaming")
    public ResponseEntity<Void> updateStreamingPreferences(
            @RequestParam(required = false) String preferredQuality,
            @RequestParam(required = false) String preferredLanguage,
            @RequestParam(required = false) Boolean autoPlay,
            @RequestParam(required = false) Boolean subtitleEnabled,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            userFeatureService.updateStreamingPreferences(userId, preferredQuality, preferredLanguage, autoPlay, subtitleEnabled);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error updating streaming preferences", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/preferences/streaming")
    public ResponseEntity<UserProfileDTO> getStreamingPreferences(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            UserProfileDTO preferences = userFeatureService.getStreamingPreferences(userId);
            return ResponseEntity.ok(preferences);
        } catch (Exception e) {
            log.error("Error getting streaming preferences", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== DOWNLOAD MANAGEMENT ====================

    @PostMapping("/download/request")
    public ResponseEntity<DownloadResponse> requestDownload(
            @Valid @RequestBody DownloadRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            DownloadResponse response = userFeatureService.requestDownload(userId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error requesting download", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/download/{movieId}/check")
    public ResponseEntity<Boolean> canDownload(
            @PathVariable Long movieId,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            boolean canDownload = userFeatureService.canDownload(userId, movieId);
            return ResponseEntity.ok(canDownload);
        } catch (Exception e) {
            log.error("Error checking download permission", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/download/history")
    public ResponseEntity<List<DownloadResponse>> getDownloadHistory(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<DownloadResponse> history = userFeatureService.getDownloadHistory(userId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error getting download history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
