package com.aimovie.service;

import com.aimovie.dto.WatchlistDTOs.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WatchlistCollectionService {

    WatchlistCollectionResponse createWatchlistCollection(Long userId, WatchlistCollectionRequest request);

    WatchlistCollectionResponse updateWatchlistCollection(Long userId, Long collectionId, WatchlistCollectionUpdateRequest request);

    WatchlistCollectionResponse getWatchlistCollectionById(Long userId, Long collectionId);

    List<WatchlistCollectionResponse> getUserWatchlistCollections(Long userId);

    List<WatchlistSummaryResponse> getUserWatchlistSummaries(Long userId);

    UserWatchlistOverviewResponse getUserWatchlistOverview(Long userId);

    WatchlistCollectionResponse getDefaultWatchlistCollection(Long userId);

    WatchlistCollectionResponse setDefaultWatchlistCollection(Long userId, Long collectionId);

    void deleteWatchlistCollection(Long userId, Long collectionId);

    List<WatchlistCollectionResponse> getPublicWatchlistCollections();

    List<WatchlistCollectionResponse> searchWatchlistCollections(Long userId, String query);

    boolean isWatchlistCollectionNameAvailable(Long userId, String name);

    WatchlistCollectionResponse duplicateWatchlistCollection(Long userId, Long sourceCollectionId, String newName);

    WatchlistCollectionResponse mergeWatchlistCollections(Long userId, Long targetCollectionId, List<Long> sourceCollectionIds);

    // Watchlist Item Management
    WatchlistItemResponse addMovieToWatchlist(Long userId, WatchlistAddRequest request);

    void removeMovieFromWatchlist(Long userId, Long collectionId, Long movieId);

    WatchlistItemResponse updateWatchlistItem(Long userId, Long collectionId, Long movieId, WatchlistItemUpdateRequest request);

    List<WatchlistItemResponse> getWatchlistItems(Long userId, Long collectionId);

    Page<WatchlistItemResponse> getWatchlistItemsPaginated(Long userId, Long collectionId, Pageable pageable);

    boolean isMovieInWatchlist(Long userId, Long collectionId, Long movieId);

    void moveMovieBetweenWatchlists(Long userId, Long fromCollectionId, Long toCollectionId, Long movieId);

    void reorderWatchlistItems(Long userId, Long collectionId, List<Long> movieIds);
}
