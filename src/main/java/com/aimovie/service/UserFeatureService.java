package com.aimovie.service;

import com.aimovie.dto.*;
import com.aimovie.dto.VideoResolutionDTOs.ResolutionChangeRequest;
import com.aimovie.dto.VideoResolutionDTOs.ResolutionChangeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserFeatureService {

    UserProfileDTO getUserProfile(Long userId);
    UserProfileDTO updateUserProfile(Long userId, UserProfileUpdateRequest request);
    void changePassword(Long userId, PasswordChangeRequest request);
    void updateAvatar(Long userId, String avatarUrl);
    UserStatsDTO getUserStats(Long userId);

    UserProfileDTO socialLogin(SocialLoginRequest request);

    Page<WatchHistoryDTO> getWatchHistory(Long userId, Pageable pageable);
    List<WatchHistoryDTO> getRecentWatchHistory(Long userId, int limit);
    List<WatchHistoryDTO> getIncompleteWatchHistory(Long userId);
    WatchHistoryDTO updateWatchHistory(Long userId, WatchHistoryUpdateRequest request);
    void deleteWatchHistory(Long userId, Long movieId);
    void clearWatchHistory(Long userId);

    Page<FavoriteDTO> getFavorites(Long userId, Pageable pageable);
    FavoriteDTO addToFavorites(Long userId, Long movieId);
    void removeFromFavorites(Long userId, Long movieId);
    boolean isFavorite(Long userId, Long movieId);
    List<FavoriteDTO> getFavoriteByGenre(Long userId, String genre, int limit);

    Page<WatchlistDTO> getWatchlist(Long userId, Pageable pageable);
    WatchlistDTO addToWatchlist(Long userId, Long movieId, String notes);
    void removeFromWatchlist(Long userId, Long movieId);
    WatchlistDTO updateWatchlistItem(Long userId, Long movieId, WatchlistUpdateRequest request);
    boolean isInWatchlist(Long userId, Long movieId);
    List<WatchlistDTO> getWatchlistByPriority(Long userId);

    MovieStreamingDTO getMovieForStreaming(Long userId, Long movieId);
    StreamingResponse startStreaming(Long userId, StreamingRequest request);
    void updateStreamingProgress(Long userId, Long movieId, Integer currentTime, Integer totalTime);

    SearchResultDTO searchMovies(Long userId, SearchRequest request);
    List<MovieSearchDTO> getTrendingMovies(Long userId, int limit);
    List<MovieSearchDTO> getFeaturedMovies(Long userId, int limit);
    List<MovieSearchDTO> getNewMovies(Long userId, int limit);
    List<MovieSearchDTO> getMoviesByGenre(Long userId, String genre, int limit);

    List<RecommendationDTO> getRecommendations(Long userId, int limit);
    List<MovieSearchDTO> getRecommendedMovies(Long userId, int limit);
    List<MovieSearchDTO> getSimilarMovies(Long userId, Long movieId, int limit);

    List<CategoryDTO> getCategories();
    CategoryDTO getCategoryByName(String categoryName);
    Page<MovieSearchDTO> getMoviesByCategory(String categoryName, Pageable pageable);

    DownloadResponse requestDownload(Long userId, DownloadRequest request);
    boolean canDownload(Long userId, Long movieId);
    List<DownloadResponse> getDownloadHistory(Long userId);

    List<SubtitleDTO> getMovieSubtitles(Long movieId);
    SubtitleDTO getDefaultSubtitle(Long movieId);
    SubtitleDTO getSubtitleByLanguage(Long movieId, String languageCode);

    void updateStreamingPreferences(Long userId, String preferredQuality, String preferredLanguage, Boolean autoPlay, Boolean subtitleEnabled);
    UserProfileDTO getStreamingPreferences(Long userId);

    // Video Resolution Management
    List<String> getAvailableQualitiesByMovieId(Long movieId);
    List<Object> getAvailableResolutionsByMovieId(Long movieId);
    String getBestAvailableQuality(Long movieId, String preferredQuality);
    boolean isResolutionAvailable(Long movieId, String quality);
    ResolutionChangeResponse changeVideoResolution(ResolutionChangeRequest request);

    List<MovieSearchDTO> getUpcomingMovies(int limit);

    List<MovieSearchDTO> getNowShowingMovies(int limit);
}
