package com.aimovie.controller;

import com.aimovie.dto.WatchlistDTO;
import com.aimovie.dto.WatchlistCollectionDTO;
import com.aimovie.service.WatchlistService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;

    @PostMapping("/movie/{movieId}")
    public ResponseEntity<WatchlistDTO> addToWatchlist(
            HttpServletRequest request,
            @PathVariable Long movieId,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "priority", required = false) Integer priority,
            @RequestParam(value = "collectionId", required = false) Long collectionId) {
        Long userId = (Long) request.getAttribute("userId");
        WatchlistDTO response = watchlistService.addToWatchlist(userId, movieId, notes, priority, collectionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<WatchlistDTO>> getUserWatchlist(
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<WatchlistDTO> response = watchlistService.getUserWatchlist(userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/movie/{movieId}")
    public ResponseEntity<Void> removeFromWatchlist(
            HttpServletRequest request,
            @PathVariable Long movieId) {
        Long userId = (Long) request.getAttribute("userId");
        watchlistService.removeFromWatchlist(userId, movieId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check/{movieId}")
    public ResponseEntity<Boolean> isInWatchlist(
            HttpServletRequest request,
            @PathVariable Long movieId) {
        Long userId = (Long) request.getAttribute("userId");
        boolean response = watchlistService.isInWatchlist(userId, movieId);
        return ResponseEntity.ok(response);
    }

    // Watchlist Collections Management
    @PostMapping("/collections")
    public ResponseEntity<WatchlistCollectionDTO> createWatchlistCollection(
            HttpServletRequest request,
            @RequestParam String name,
            @RequestParam(required = false) String description) {
        Long userId = (Long) request.getAttribute("userId");
        WatchlistCollectionDTO response = watchlistService.createWatchlistCollection(userId, name, description);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/collections")
    public ResponseEntity<List<WatchlistCollectionDTO>> getUserWatchlistCollections(
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<WatchlistCollectionDTO> response = watchlistService.getUserWatchlistCollections(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/collections/{collectionId}")
    public ResponseEntity<WatchlistCollectionDTO> getWatchlistCollection(
            HttpServletRequest request,
            @PathVariable Long collectionId) {
        Long userId = (Long) request.getAttribute("userId");
        WatchlistCollectionDTO response = watchlistService.getWatchlistCollection(userId, collectionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/collections/{collectionId}/movies")
    public ResponseEntity<List<WatchlistDTO>> getWatchlistCollectionMovies(
            HttpServletRequest request,
            @PathVariable Long collectionId) {
        Long userId = (Long) request.getAttribute("userId");
        List<WatchlistDTO> response = watchlistService.getWatchlistCollectionMovies(userId, collectionId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/collections/{collectionId}")
    public ResponseEntity<WatchlistCollectionDTO> updateWatchlistCollection(
            HttpServletRequest request,
            @PathVariable Long collectionId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description) {
        Long userId = (Long) request.getAttribute("userId");
        WatchlistCollectionDTO response = watchlistService.updateWatchlistCollection(userId, collectionId, name, description);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/collections/{collectionId}")
    public ResponseEntity<Void> deleteWatchlistCollection(
            HttpServletRequest request,
            @PathVariable Long collectionId) {
        Long userId = (Long) request.getAttribute("userId");
        watchlistService.deleteWatchlistCollection(userId, collectionId);
        return ResponseEntity.ok().build();
    }
}
