package com.aimovie.controller;

import com.aimovie.dto.ApiResponse;
import com.aimovie.dto.WatchlistDTOs.*;
import com.aimovie.service.WatchlistCollectionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/watchlist-collections")
@RequiredArgsConstructor
@Slf4j
public class WatchlistCollectionController {

    private final WatchlistCollectionService watchlistCollectionService;

    // WatchlistCollection Management
    @PostMapping
    public ResponseEntity<ApiResponse<WatchlistCollectionResponse>> createWatchlistCollection(
            @Valid @RequestBody WatchlistCollectionRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            WatchlistCollectionResponse response = watchlistCollectionService.createWatchlistCollection(userId, request);
            ApiResponse<WatchlistCollectionResponse> apiResponse = new ApiResponse<>("SUCCESS", "Watchlist collection created successfully", response);
            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error creating watchlist collection: {}", e.getMessage());
            ApiResponse<WatchlistCollectionResponse> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.badRequest().body(apiResponse);
        } catch (Exception e) {
            log.error("Error creating watchlist collection", e);
            ApiResponse<WatchlistCollectionResponse> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @PutMapping("/{collectionId}")
    public ResponseEntity<ApiResponse<WatchlistCollectionResponse>> updateWatchlistCollection(
            @PathVariable Long collectionId,
            @Valid @RequestBody WatchlistCollectionUpdateRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            WatchlistCollectionResponse response = watchlistCollectionService.updateWatchlistCollection(userId, collectionId, request);
            ApiResponse<WatchlistCollectionResponse> apiResponse = new ApiResponse<>("SUCCESS", "Watchlist collection updated successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error updating watchlist collection: {}", e.getMessage());
            ApiResponse<WatchlistCollectionResponse> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.badRequest().body(apiResponse);
        } catch (Exception e) {
            log.error("Error updating watchlist collection", e);
            ApiResponse<WatchlistCollectionResponse> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/{collectionId}")
    public ResponseEntity<ApiResponse<WatchlistCollectionResponse>> getWatchlistCollection(
            @PathVariable Long collectionId,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            WatchlistCollectionResponse response = watchlistCollectionService.getWatchlistCollectionById(userId, collectionId);
            ApiResponse<WatchlistCollectionResponse> apiResponse = new ApiResponse<>("SUCCESS", "Watchlist collection retrieved successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error getting watchlist collection: {}", e.getMessage());
            ApiResponse<WatchlistCollectionResponse> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting watchlist collection", e);
            ApiResponse<WatchlistCollectionResponse> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WatchlistCollectionResponse>>> getUserWatchlistCollections(
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            List<WatchlistCollectionResponse> response = watchlistCollectionService.getUserWatchlistCollections(userId);
            ApiResponse<List<WatchlistCollectionResponse>> apiResponse = new ApiResponse<>("SUCCESS", "Watchlist collections retrieved successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error getting user watchlist collections", e);
            ApiResponse<List<WatchlistCollectionResponse>> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/summaries")
    public ResponseEntity<ApiResponse<List<WatchlistSummaryResponse>>> getUserWatchlistSummaries(
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            List<WatchlistSummaryResponse> response = watchlistCollectionService.getUserWatchlistSummaries(userId);
            ApiResponse<List<WatchlistSummaryResponse>> apiResponse = new ApiResponse<>("SUCCESS", "Watchlist summaries retrieved successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error getting user watchlist summaries", e);
            ApiResponse<List<WatchlistSummaryResponse>> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<UserWatchlistOverviewResponse>> getUserWatchlistOverview(
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            UserWatchlistOverviewResponse response = watchlistCollectionService.getUserWatchlistOverview(userId);
            ApiResponse<UserWatchlistOverviewResponse> apiResponse = new ApiResponse<>("SUCCESS", "Watchlist overview retrieved successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error getting user watchlist overview", e);
            ApiResponse<UserWatchlistOverviewResponse> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/default")
    public ResponseEntity<ApiResponse<WatchlistCollectionResponse>> getDefaultWatchlistCollection(
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            WatchlistCollectionResponse response = watchlistCollectionService.getDefaultWatchlistCollection(userId);
            ApiResponse<WatchlistCollectionResponse> apiResponse = new ApiResponse<>("SUCCESS", "Default watchlist collection retrieved successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error getting default watchlist collection: {}", e.getMessage());
            ApiResponse<WatchlistCollectionResponse> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting default watchlist collection", e);
            ApiResponse<WatchlistCollectionResponse> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @PutMapping("/{collectionId}/set-default")
    public ResponseEntity<ApiResponse<WatchlistCollectionResponse>> setDefaultWatchlistCollection(
            @PathVariable Long collectionId,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            WatchlistCollectionResponse response = watchlistCollectionService.setDefaultWatchlistCollection(userId, collectionId);
            ApiResponse<WatchlistCollectionResponse> apiResponse = new ApiResponse<>("SUCCESS", "Default watchlist collection set successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error setting default watchlist collection: {}", e.getMessage());
            ApiResponse<WatchlistCollectionResponse> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.badRequest().body(apiResponse);
        } catch (Exception e) {
            log.error("Error setting default watchlist collection", e);
            ApiResponse<WatchlistCollectionResponse> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @DeleteMapping("/{collectionId}")
    public ResponseEntity<ApiResponse<Void>> deleteWatchlistCollection(
            @PathVariable Long collectionId,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            watchlistCollectionService.deleteWatchlistCollection(userId, collectionId);
            ApiResponse<Void> apiResponse = new ApiResponse<>("SUCCESS", "Watchlist collection deleted successfully", null);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error deleting watchlist collection: {}", e.getMessage());
            ApiResponse<Void> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.badRequest().body(apiResponse);
        } catch (Exception e) {
            log.error("Error deleting watchlist collection", e);
            ApiResponse<Void> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<WatchlistCollectionResponse>>> getPublicWatchlistCollections() {
        try {
            List<WatchlistCollectionResponse> response = watchlistCollectionService.getPublicWatchlistCollections();
            ApiResponse<List<WatchlistCollectionResponse>> apiResponse = new ApiResponse<>("SUCCESS", "Public watchlist collections retrieved successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error getting public watchlist collections", e);
            ApiResponse<List<WatchlistCollectionResponse>> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<WatchlistCollectionResponse>>> searchWatchlistCollections(
            @RequestParam String query,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            List<WatchlistCollectionResponse> response = watchlistCollectionService.searchWatchlistCollections(userId, query);
            ApiResponse<List<WatchlistCollectionResponse>> apiResponse = new ApiResponse<>("SUCCESS", "Search results retrieved successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error searching watchlist collections", e);
            ApiResponse<List<WatchlistCollectionResponse>> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/check-name")
    public ResponseEntity<ApiResponse<Boolean>> isWatchlistCollectionNameAvailable(
            @RequestParam String name,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            Boolean response = watchlistCollectionService.isWatchlistCollectionNameAvailable(userId, name);
            ApiResponse<Boolean> apiResponse = new ApiResponse<>("SUCCESS", "Name availability checked", response);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error checking watchlist collection name availability", e);
            ApiResponse<Boolean> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @PostMapping("/{sourceCollectionId}/duplicate")
    public ResponseEntity<ApiResponse<WatchlistCollectionResponse>> duplicateWatchlistCollection(
            @PathVariable Long sourceCollectionId,
            @RequestParam String newName,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            WatchlistCollectionResponse response = watchlistCollectionService.duplicateWatchlistCollection(userId, sourceCollectionId, newName);
            ApiResponse<WatchlistCollectionResponse> apiResponse = new ApiResponse<>("SUCCESS", "Watchlist collection duplicated successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error duplicating watchlist collection: {}", e.getMessage());
            ApiResponse<WatchlistCollectionResponse> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.badRequest().body(apiResponse);
        } catch (Exception e) {
            log.error("Error duplicating watchlist collection", e);
            ApiResponse<WatchlistCollectionResponse> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @PostMapping("/{targetCollectionId}/merge")
    public ResponseEntity<ApiResponse<WatchlistCollectionResponse>> mergeWatchlistCollections(
            @PathVariable Long targetCollectionId,
            @RequestBody List<Long> sourceCollectionIds,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            WatchlistCollectionResponse response = watchlistCollectionService.mergeWatchlistCollections(userId, targetCollectionId, sourceCollectionIds);
            ApiResponse<WatchlistCollectionResponse> apiResponse = new ApiResponse<>("SUCCESS", "Watchlist collections merged successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error merging watchlist collections: {}", e.getMessage());
            ApiResponse<WatchlistCollectionResponse> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.badRequest().body(apiResponse);
        } catch (Exception e) {
            log.error("Error merging watchlist collections", e);
            ApiResponse<WatchlistCollectionResponse> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    // Watchlist Item Management
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<WatchlistItemResponse>> addMovieToWatchlist(
            @Valid @RequestBody WatchlistAddRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            WatchlistItemResponse response = watchlistCollectionService.addMovieToWatchlist(userId, request);
            ApiResponse<WatchlistItemResponse> apiResponse = new ApiResponse<>("SUCCESS", "Movie added to watchlist successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error adding movie to watchlist: {}", e.getMessage());
            ApiResponse<WatchlistItemResponse> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.badRequest().body(apiResponse);
        } catch (Exception e) {
            log.error("Error adding movie to watchlist", e);
            ApiResponse<WatchlistItemResponse> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @DeleteMapping("/{collectionId}/movies/{movieId}")
    public ResponseEntity<ApiResponse<Void>> removeMovieFromWatchlist(
            @PathVariable Long collectionId,
            @PathVariable Long movieId,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            watchlistCollectionService.removeMovieFromWatchlist(userId, collectionId, movieId);
            ApiResponse<Void> apiResponse = new ApiResponse<>("SUCCESS", "Movie removed from watchlist successfully", null);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error removing movie from watchlist: {}", e.getMessage());
            ApiResponse<Void> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.badRequest().body(apiResponse);
        } catch (Exception e) {
            log.error("Error removing movie from watchlist", e);
            ApiResponse<Void> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @PutMapping("/{collectionId}/movies/{movieId}")
    public ResponseEntity<ApiResponse<WatchlistItemResponse>> updateWatchlistItem(
            @PathVariable Long collectionId,
            @PathVariable Long movieId,
            @Valid @RequestBody WatchlistItemUpdateRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            WatchlistItemResponse response = watchlistCollectionService.updateWatchlistItem(userId, collectionId, movieId, request);
            ApiResponse<WatchlistItemResponse> apiResponse = new ApiResponse<>("SUCCESS", "Watchlist item updated successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error updating watchlist item: {}", e.getMessage());
            ApiResponse<WatchlistItemResponse> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.badRequest().body(apiResponse);
        } catch (Exception e) {
            log.error("Error updating watchlist item", e);
            ApiResponse<WatchlistItemResponse> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/{collectionId}/movies")
    public ResponseEntity<ApiResponse<List<WatchlistItemResponse>>> getWatchlistItems(
            @PathVariable Long collectionId,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            List<WatchlistItemResponse> response = watchlistCollectionService.getWatchlistItems(userId, collectionId);
            ApiResponse<List<WatchlistItemResponse>> apiResponse = new ApiResponse<>("SUCCESS", "Watchlist items retrieved successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error getting watchlist items: {}", e.getMessage());
            ApiResponse<List<WatchlistItemResponse>> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting watchlist items", e);
            ApiResponse<List<WatchlistItemResponse>> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/{collectionId}/movies/paginated")
    public ResponseEntity<ApiResponse<Page<WatchlistItemResponse>>> getWatchlistItemsPaginated(
            @PathVariable Long collectionId,
            Pageable pageable,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            Page<WatchlistItemResponse> response = watchlistCollectionService.getWatchlistItemsPaginated(userId, collectionId, pageable);
            ApiResponse<Page<WatchlistItemResponse>> apiResponse = new ApiResponse<>("SUCCESS", "Watchlist items retrieved successfully", response);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error getting paginated watchlist items: {}", e.getMessage());
            ApiResponse<Page<WatchlistItemResponse>> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting paginated watchlist items", e);
            ApiResponse<Page<WatchlistItemResponse>> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping("/{collectionId}/movies/{movieId}/check")
    public ResponseEntity<ApiResponse<Boolean>> isMovieInWatchlist(
            @PathVariable Long collectionId,
            @PathVariable Long movieId,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            Boolean response = watchlistCollectionService.isMovieInWatchlist(userId, collectionId, movieId);
            ApiResponse<Boolean> apiResponse = new ApiResponse<>("SUCCESS", "Movie watchlist status checked", response);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Error checking movie in watchlist", e);
            ApiResponse<Boolean> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @PostMapping("/{fromCollectionId}/movies/{movieId}/move/{toCollectionId}")
    public ResponseEntity<ApiResponse<Void>> moveMovieBetweenWatchlists(
            @PathVariable Long fromCollectionId,
            @PathVariable Long movieId,
            @PathVariable Long toCollectionId,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            watchlistCollectionService.moveMovieBetweenWatchlists(userId, fromCollectionId, toCollectionId, movieId);
            ApiResponse<Void> apiResponse = new ApiResponse<>("SUCCESS", "Movie moved between watchlists successfully", null);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error moving movie between watchlists: {}", e.getMessage());
            ApiResponse<Void> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.badRequest().body(apiResponse);
        } catch (Exception e) {
            log.error("Error moving movie between watchlists", e);
            ApiResponse<Void> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @PutMapping("/{collectionId}/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderWatchlistItems(
            @PathVariable Long collectionId,
            @RequestBody List<Long> movieIds,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            watchlistCollectionService.reorderWatchlistItems(userId, collectionId, movieIds);
            ApiResponse<Void> apiResponse = new ApiResponse<>("SUCCESS", "Watchlist items reordered successfully", null);
            return ResponseEntity.ok(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error reordering watchlist items: {}", e.getMessage());
            ApiResponse<Void> apiResponse = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.badRequest().body(apiResponse);
        } catch (Exception e) {
            log.error("Error reordering watchlist items", e);
            ApiResponse<Void> apiResponse = new ApiResponse<>("ERROR", "Internal server error", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
}
