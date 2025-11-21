package com.aimovie.serviceImpl;

import com.aimovie.dto.*;
import com.aimovie.dto.VideoResolutionDTOs.ResolutionChangeRequest;
import com.aimovie.dto.VideoResolutionDTOs.ResolutionChangeResponse;
import com.aimovie.dto.VideoResolutionDTOs.AvailableResolutionResponse;
import com.aimovie.entity.*;
import com.aimovie.repository.*;
import com.aimovie.entity.WatchlistCollection;
import com.aimovie.service.UserFeatureService;
import com.aimovie.service.VideoResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.bytedeco.javacv.FFmpegFrameGrabber;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserFeatureServiceImpl implements UserFeatureService {

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final WatchHistoryRepository watchHistoryRepository;
    private final FavoriteRepository favoriteRepository;
    private final WatchlistRepository watchlistRepository;
    private final WatchlistCollectionRepository watchlistCollectionRepository;
    private final SubtitleRepository subtitleRepository;
    private final VideoResolutionRepository videoResolutionRepository;
    private final PasswordEncoder passwordEncoder;
    private final VideoResolutionService videoResolutionService;

    @Override
    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return convertToUserProfileDTO(user);
    }

    @Override
    public UserProfileDTO updateUserProfile(Long userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getBirthday() != null) user.setBirthday(request.getBirthday());
        if (request.getPreferredLanguage() != null) user.setPreferredLanguage(request.getPreferredLanguage());
        if (request.getPreferredQuality() != null) user.setPreferredQuality(request.getPreferredQuality());
        if (request.getAutoPlay() != null) user.setAutoPlay(request.getAutoPlay());
        if (request.getSubtitleEnabled() != null) user.setSubtitleEnabled(request.getSubtitleEnabled());
        
        user = userRepository.save(user);
        return convertToUserProfileDTO(user);
    }

    @Override
    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
        }
        
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserStatsDTO getUserStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        long totalWatchTime = watchHistoryRepository.findByUser(user, Pageable.unpaged())
                .getContent()
                .stream()
                .mapToLong(wh -> wh.getWatchDurationSeconds() != null ? wh.getWatchDurationSeconds() : 0)
                .sum();
        
        long totalMoviesWatched = watchHistoryRepository.countByUserAndIsCompletedTrue(user);
        long totalFavorites = favoriteRepository.countByUserAndIsFavoriteTrue(user);
        long totalWatchlistItems = watchlistRepository.countByUserIdAndIsInWatchlistTrue(user.getId());
        
        return UserStatsDTO.builder()
                .totalWatchTime(totalWatchTime)
                .totalMoviesWatched(totalMoviesWatched)
                .totalFavorites(totalFavorites)
                .totalWatchlistItems(totalWatchlistItems)
                .build();
    }

    // Social Login
    @Override
    public UserProfileDTO socialLogin(SocialLoginRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setLastLoginAt(LocalDateTime.now());
            user = userRepository.save(user);
            return convertToUserProfileDTO(user);
        } else {
            // Create new user
            User newUser = User.builder()
                    .username(request.getEmail().split("@")[0])
                    .email(request.getEmail())
                    .fullName(request.getFullName())
                    .avatarUrl(request.getAvatarUrl())
                    .authProvider(User.AuthProvider.valueOf(request.getProvider().toUpperCase()))
                    .providerId(request.getProviderId())
                    .emailVerified(true)
                    .enabled(true)
                    .roles(Set.of(Role.USER))
                    .lastLoginAt(LocalDateTime.now())
                    .build();
            
            newUser = userRepository.save(newUser);
            return convertToUserProfileDTO(newUser);
        }
    }

    // Watch History Management
    @Override
    @Transactional(readOnly = true)
    public Page<WatchHistoryDTO> getWatchHistory(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        return watchHistoryRepository.findByUserOrderByLastWatchedAtDesc(user, pageable)
                .map(this::convertToWatchHistoryDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WatchHistoryDTO> getRecentWatchHistory(Long userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        return watchHistoryRepository.findByUserOrderByLastWatchedAtDesc(user, Pageable.ofSize(limit))
                .getContent()
                .stream()
                .map(this::convertToWatchHistoryDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WatchHistoryDTO> getIncompleteWatchHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        return watchHistoryRepository.findByUserAndIsCompletedFalseOrderByLastWatchedAtDesc(user)
                .stream()
                .map(this::convertToWatchHistoryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public WatchHistoryDTO updateWatchHistory(Long userId, WatchHistoryUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + request.getMovieId()));
        
        Optional<WatchHistory> existingHistory = watchHistoryRepository.findByUserAndMovie(user, movie);
        
        WatchHistory watchHistory;
        if (existingHistory.isPresent()) {
            watchHistory = existingHistory.get();
        } else {
            watchHistory = WatchHistory.builder()
                    .user(user)
                    .movie(movie)
                    .build();
        }
        
        watchHistory.setWatchDurationSeconds(request.getWatchDurationSeconds());
        watchHistory.setTotalDurationSeconds(request.getTotalDurationSeconds());
        watchHistory.setDeviceType(request.getDeviceType());
        watchHistory.setQuality(request.getQuality());
        watchHistory.setSubtitleLanguage(request.getSubtitleLanguage());
        watchHistory.setVolumeLevel(request.getVolumeLevel() != null ? request.getVolumeLevel().doubleValue() : null);
        watchHistory.setPlaybackSpeed(request.getPlaybackSpeed());
        watchHistory.setLastWatchedAt(LocalDateTime.now());
        
        if (request.getTotalDurationSeconds() != null && request.getTotalDurationSeconds() > 0) {
            double percentage = (double) request.getWatchDurationSeconds() / request.getTotalDurationSeconds() * 100;
            watchHistory.setWatchPercentage(percentage);
            watchHistory.setIsCompleted(percentage >= 90.0); 
        }
        
        watchHistory = watchHistoryRepository.save(watchHistory);
        return convertToWatchHistoryDTO(watchHistory);
    }

    @Override
    public void deleteWatchHistory(Long userId, Long movieId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));
        
        watchHistoryRepository.findByUserAndMovie(user, movie)
                .ifPresent(watchHistoryRepository::delete);
    }

    @Override
    public void clearWatchHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        watchHistoryRepository.deleteByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FavoriteDTO> getFavorites(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        return favoriteRepository.findByUserAndIsFavoriteTrueOrderByAddedAtDesc(user, pageable)
                .map(this::convertToFavoriteDTO);
    }

    @Override
    public FavoriteDTO addToFavorites(Long userId, Long movieId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));
        
        Optional<Favorite> existingFavorite = favoriteRepository.findByUserAndMovie(user, movie);
        
        Favorite favorite;
        if (existingFavorite.isPresent()) {
            favorite = existingFavorite.get();
            favorite.setIsFavorite(true);
        } else {
            favorite = Favorite.builder()
                    .user(user)
                    .movie(movie)
                    .isFavorite(true)
                    .addedAt(LocalDateTime.now())
                    .build();
        }
        
        favorite = favoriteRepository.save(favorite);
        return convertToFavoriteDTO(favorite);
    }

    @Override
    public void removeFromFavorites(Long userId, Long movieId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));
        
        favoriteRepository.findByUserAndMovie(user, movie)
                .ifPresent(favorite -> {
                    favorite.setIsFavorite(false);
                    favoriteRepository.save(favorite);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorite(Long userId, Long movieId) {
        if (userId == null) {
            return false; 
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));
        
        return favoriteRepository.existsByUserAndMovieAndIsFavoriteTrue(user, movie);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WatchlistDTO> getWatchlist(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        return watchlistRepository.findByUserIdAndIsInWatchlistTrueOrderByPriorityDescAddedAtDesc(user.getId(), pageable)
                .map(this::convertToWatchlistDTO);
    }

    @Override
    public WatchlistDTO addToWatchlist(Long userId, Long movieId, String notes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));
        
        Optional<Watchlist> existingWatchlist = watchlistRepository.findByUserIdAndMovieId(user.getId(), movie.getId());
        
        Watchlist watchlist;
        if (existingWatchlist.isPresent()) {
            watchlist = existingWatchlist.get();
            watchlist.setIsInWatchlist(true);
        } else {
            watchlist = Watchlist.builder()
                    .watchlistCollection(getOrCreateDefaultWatchlistCollection(user))
                    .movie(movie)
                    .isInWatchlist(true)
                    .notes(notes)
                    .addedAt(LocalDateTime.now())
                    .build();
        }
        
        watchlist = watchlistRepository.save(watchlist);
        return convertToWatchlistDTO(watchlist);
    }

    @Override
    public void removeFromWatchlist(Long userId, Long movieId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));
        
        watchlistRepository.findByUserIdAndMovieId(user.getId(), movie.getId())
                .ifPresent(watchlist -> {
                    watchlist.setIsInWatchlist(false);
                    watchlistRepository.save(watchlist);
                });
    }

    @Override
    public WatchlistDTO updateWatchlistItem(Long userId, Long movieId, WatchlistUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));
        
        Watchlist watchlist = watchlistRepository.findByUserIdAndMovieId(user.getId(), movie.getId())
                .orElseThrow(() -> new RuntimeException("Movie not found in watchlist"));
        
        if (request.getPriority() != null) watchlist.setPriority(request.getPriority());
        if (request.getNotes() != null) watchlist.setNotes(request.getNotes());
        
        watchlist = watchlistRepository.save(watchlist);
        return convertToWatchlistDTO(watchlist);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isInWatchlist(Long userId, Long movieId) {
        if (userId == null) {
            return false; 
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));
        
        return watchlistRepository.existsByUserIdAndMovieIdAndIsInWatchlistTrue(user.getId(), movie.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public MovieStreamingDTO getMovieForStreaming(Long userId, Long movieId) {
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        }

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));
        
        Optional<WatchHistory> watchHistory = (user != null)
                ? watchHistoryRepository.findByUserAndMovie(user, movie)
                : Optional.empty();
        Integer resumeTime = watchHistory.map(WatchHistory::getWatchDurationSeconds).orElse(0);
        
        // Get subtitles
        List<SubtitleDTO> subtitles = getMovieSubtitles(movieId);
        
        boolean isInWatchlist = isInWatchlist(userId, movieId);
        boolean isFavorite = isFavorite(userId, movieId);
        
        return MovieStreamingDTO.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .synopsis(movie.getSynopsis())
                .year(movie.getYear())
                .actors(movie.getActors())
                .directorName(movie.getDirector() != null ? movie.getDirector().getName() : null)
                .country(movie.getCountry() != null ? movie.getCountry().getName() : null)
                .language(movie.getLanguage())
                .ageRating(movie.getAgeRating())
                .imdbRating(movie.getImdbRating())
                .averageRating(movie.getAverageRating())
                .viewCount(movie.getViewCount())
                .posterUrl(movie.getPosterUrl())
                .trailerUrl(movie.getTrailerUrl())
                .availableQualities(movie.getAvailableQualities())
                .subtitles(subtitles)
                .downloadEnabled(movie.getDownloadEnabled())
                .maxDownloadQuality(movie.getMaxDownloadQuality())
                .videoDuration(movie.getVideoDuration())
                .currentQuality(user != null ? user.getPreferredQuality() : null)
                .currentSubtitleLanguage(user != null ? user.getPreferredLanguage() : null)
                .subtitleEnabled(user != null ? user.getSubtitleEnabled() : Boolean.FALSE)
                .totalDuration(movie.getVideoDuration())
                .currentPosition(resumeTime)
                .resumeTime(resumeTime)
                .isCompleted(watchHistory.map(h -> h.getWatchDurationSeconds() >= movie.getVideoDuration()).orElse(false))
                .isInWatchlist(isInWatchlist)
                .isFavorite(isFavorite)
                .build();
    }

    @Override
    public StreamingResponse startStreaming(Long userId, StreamingRequest request) {
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        }
        
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + request.getMovieId()));
        
        movie.setViewCount(movie.getViewCount() + 1);
        movieRepository.save(movie);
        
        List<String> availableQualities = resolveAvailableQualities(movie);
        String chosenQuality = resolveRequestedQuality(availableQualities, request.getQuality());

        String streamingUrl = getStreamingUrl(movie, chosenQuality);
        if (streamingUrl == null) {
            throw new RuntimeException("Streaming URL is not available for movie id: " + movie.getId());
        }

        if (movie.getVideoDuration() == null) {
            try {
                String originalUrl = movie.getVideoUrl();
                if (originalUrl != null && !originalUrl.isBlank()) {
                    String filename = originalUrl.substring(originalUrl.lastIndexOf('/') + 1);
                    Path originalPath = Paths.get("uploads", "videos", filename);
                    if (Files.exists(originalPath)) {
                        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(originalPath.toFile())) {
                            grabber.start();
                            long durationSec = Math.max(1, grabber.getLengthInTime() / 1_000_000);
                            grabber.stop();
                            movie.setVideoDuration((int) Math.round(durationSec / 60.0));
                            movieRepository.save(movie);
                        }
                    }
                }
            } catch (Exception ignored) { }
        }
        
        List<SubtitleDTO> availableSubtitles = getMovieSubtitles(request.getMovieId());
        String subtitleLanguage = resolveSubtitleLanguage(request.getSubtitleLanguage(), availableSubtitles);
        String subtitleUrl = resolveSubtitleUrl(subtitleLanguage, availableSubtitles);
        Boolean subtitleEnabled = request.getSubtitleEnabled();
        if (subtitleEnabled == null) {
            subtitleEnabled = user != null ? user.getSubtitleEnabled() : Boolean.TRUE;
        }
        
        Optional<WatchHistory> watchHistory = (user != null)
                ? watchHistoryRepository.findByUserAndMovie(user, movie)
                : Optional.empty();

        Integer currentPosition = watchHistory.map(WatchHistory::getWatchDurationSeconds).orElse(0);
        Boolean isCompleted = watchHistory.map(WatchHistory::getIsCompleted).orElse(false);

        return StreamingResponse.builder()
                .movieId(movie.getId())
                .title(movie.getTitle())
                .streamingUrl(streamingUrl)
                .streamUrl(streamingUrl)
                .quality(chosenQuality)
                .subtitleUrl(subtitleUrl)
                .subtitleLanguage(subtitleLanguage)
                .currentSubtitleLanguage(subtitleLanguage)
                .subtitleEnabled(subtitleEnabled)
                .totalDuration(movie.getVideoDuration())
                .availableQualities(availableQualities)
                .availableSubtitles(availableSubtitles)
                .subtitles(availableSubtitles)
                .canDownload(movie.getDownloadEnabled())
                .downloadUrl(movie.getDownloadEnabled() ? getDownloadUrl(movie, chosenQuality) : null)
                .maxDownloadQuality(movie.getMaxDownloadQuality())
                .posterUrl(movie.getPosterUrl())
                .trailerUrl(movie.getTrailerUrl())
                .currentPosition(currentPosition)
                .isCompleted(isCompleted)
                .build();
    }

    @Override
    public void updateStreamingProgress(Long userId, Long movieId, Integer currentTime, Integer totalTime) {
        WatchHistoryUpdateRequest request = WatchHistoryUpdateRequest.builder()
                .movieId(movieId)
                .watchDurationSeconds(currentTime)
                .totalDurationSeconds(totalTime)
                .build();
        
        updateWatchHistory(userId, request);
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResultDTO searchMovies(Long userId, SearchRequest request) {
        try {
            Pageable pageable = PageRequest.of(0, request.getSize() != null ? request.getSize() : 20);
            Page<Movie> moviePage;

            LocalDate today = LocalDate.now();
            
            if (request.getQuery() != null && !request.getQuery().trim().isEmpty()) {
                moviePage = movieRepository.findByTitleOrSynopsisContainingIgnoreCaseAndReleased(
                    request.getQuery(), request.getQuery(), today, pageable);
            } else if (request.getActors() != null && !request.getActors().isEmpty()) {
                moviePage = movieRepository.findByActorsLikeIgnoreCaseAndReleased(
                    request.getActors().get(0), today, pageable);
            } else if (request.getDirectors() != null && !request.getDirectors().isEmpty()) {
                moviePage = movieRepository.findByDirectorNameContainingIgnoreCaseAndReleased(
                    request.getDirectors().get(0), today, pageable);
            } else if (request.getYearFrom() != null && request.getYearTo() != null) {
                moviePage = movieRepository.findByYearBetweenAndReleased(
                    request.getYearFrom(), request.getYearTo(), today, pageable);
            } else if (request.getMinRating() != null || request.getMaxRating() != null) {
                double minRating = request.getMinRating() != null ? request.getMinRating() : 0.0;
                double maxRating = request.getMaxRating() != null ? request.getMaxRating() : 10.0;
                moviePage = movieRepository.findByAverageRatingBetweenAndReleased(minRating, maxRating, today, pageable);
            } else {
                moviePage = movieRepository.findByIsAvailableTrueAndReleased(today, pageable);
            }

            List<MovieSearchDTO> movies = moviePage.getContent().stream()
                    .map(movie -> convertToMovieSearchDTO(movie, userId))
                    .collect(Collectors.toList());

            return SearchResultDTO.builder()
                    .movies(movies)
                    .totalElements(moviePage.getTotalElements())
                    .totalPages(moviePage.getTotalPages())
                    .currentPage(0)
                    .size(movies.size())
                    .searchQuery(request.getQuery())
                    .build();
        } catch (Exception e) {
            log.error("Error searching movies", e);
            return SearchResultDTO.builder()
                    .movies(List.of())
                    .totalElements(0L)
                    .totalPages(0)
                    .currentPage(0)
                    .size(0)
                    .searchQuery(request.getQuery())
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieSearchDTO> getTrendingMovies(Long userId, int limit) {
        return movieRepository.findTrendingNowShowing(LocalDate.now(), PageRequest.of(0, limit))
                .getContent()
                .stream()
                .map(movie -> convertToMovieSearchDTO(movie, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieSearchDTO> getFeaturedMovies(Long userId, int limit) {
        return movieRepository.findByIsFeaturedTrueAndIsAvailableTrueAndReleased(LocalDate.now(), PageRequest.of(0, limit))
                .getContent()
                .stream()
                .map(movie -> convertToMovieSearchDTO(movie, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieSearchDTO> getUpcomingMovies(int limit) {
        return movieRepository.findUpcomingByReleaseDate(LocalDate.now(), PageRequest.of(0, limit))
                .getContent()
                .stream()
                .map(movie -> convertToMovieSearchDTO(movie, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieSearchDTO> getNowShowingMovies(int limit) {
        return movieRepository.findNowShowingByReleaseDate(LocalDate.now(), PageRequest.of(0, limit))
                .getContent()
                .stream()
                .map(movie -> convertToMovieSearchDTO(movie, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecommendationDTO> getRecommendations(Long userId, int limit) {
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategories() {
        List<CategoryDTO> categories = new ArrayList<>();

        // Special categories
        LocalDate today = LocalDate.now();
        
        List<MovieSearchDTO> featured = getFeaturedMovies(null, 10);
        categories.add(CategoryDTO.builder()
                .name("featured")
                .displayName("Featured")
                .type("FEATURED")
                .movies(featured)
                .totalMovies(movieRepository.findByIsFeaturedTrueAndIsAvailableTrueAndReleased(today, PageRequest.of(0, 1)).getTotalElements())
                .build());

        List<MovieSearchDTO> trending = getTrendingMovies(null, 10);
        categories.add(CategoryDTO.builder()
                .name("trending")
                .displayName("Trending")
                .type("TRENDING")
                .movies(trending)
                .totalMovies(movieRepository.findTrendingNowShowing(today, PageRequest.of(0, 1)).getTotalElements())
                .build());

        List<MovieSearchDTO> newest = getNewMovies(null, 10);
        categories.add(CategoryDTO.builder()
                .name("new")
                .displayName("New Releases")
                .type("NEW")
                .movies(newest)
                .totalMovies(movieRepository.findByIsAvailableTrueAndReleased(today, PageRequest.of(0, 1)).getTotalElements())
                .build());

        // Genre categories removed

        return categories;
    }

    @Override
    public DownloadResponse requestDownload(Long userId, DownloadRequest request) {
        return DownloadResponse.builder()
                .downloadUrl("")
                .fileName("")
                .fileSize(0L)
                .quality(request.getQuality())
                .format(request.getFormat())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDownload(Long userId, Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));
        return movie.getDownloadEnabled();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubtitleDTO> getMovieSubtitles(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));
        
        return subtitleRepository.findByMovieAndIsAvailableTrueOrderByIsDefaultDescLanguageNameAsc(movie)
                .stream()
                .map(this::convertToSubtitleDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SubtitleDTO getDefaultSubtitle(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));
        
        return subtitleRepository.findByMovieAndIsDefaultTrueAndIsAvailableTrue(movie)
                .map(this::convertToSubtitleDTO)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public SubtitleDTO getSubtitleByLanguage(Long movieId, String languageCode) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));
        
        return subtitleRepository.findByMovieAndLanguageCodeAndIsAvailableTrue(movie, languageCode)
                .map(this::convertToSubtitleDTO)
                .orElse(null);
    }

    @Override
    public void updateStreamingPreferences(Long userId, String preferredQuality, String preferredLanguage, Boolean autoPlay, Boolean subtitleEnabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        if (preferredQuality != null) user.setPreferredQuality(preferredQuality);
        if (preferredLanguage != null) user.setPreferredLanguage(preferredLanguage);
        if (autoPlay != null) user.setAutoPlay(autoPlay);
        if (subtitleEnabled != null) user.setSubtitleEnabled(subtitleEnabled);
        
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDTO getStreamingPreferences(Long userId) {
        return getUserProfile(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DownloadResponse> getDownloadHistory(Long userId) {
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WatchlistDTO> getWatchlistByPriority(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        return watchlistRepository.findByUserIdAndIsInWatchlistTrueOrderByPriorityDesc(user.getId())
                .stream()
                .map(this::convertToWatchlistDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieSearchDTO> getMoviesByGenre(Long userId, String genre, int limit) {
        // Genres removed, return empty list
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieSearchDTO> getRecommendedMovies(Long userId, int limit) {
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteDTO> getFavoriteByGenre(Long userId, String genre, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Genres removed, return empty list
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieSearchDTO> getMoviesByCategory(String categoryName, Pageable pageable) {
        if (categoryName == null || categoryName.isBlank()) {
            return Page.empty();
        }

        String normalized = categoryName.trim().toLowerCase();

        LocalDate today = LocalDate.now();

        if (normalized.equals("featured")) {
            Page<Movie> page = movieRepository.findByIsFeaturedTrueAndIsAvailableTrueAndReleased(today, pageable);
            return page.map(movie -> convertToMovieSearchDTO(movie, null));
        }
        if (normalized.equals("trending")) {
            Page<Movie> page = movieRepository.findTrendingNowShowing(today, pageable);
            return page.map(movie -> convertToMovieSearchDTO(movie, null));
        }
        if (normalized.equals("new")) {
            Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("releaseDate").descending().and(Sort.by("createdAt").descending()));
            Page<Movie> page = movieRepository.findByIsAvailableTrueAndReleased(today, sorted);
            return page.map(movie -> convertToMovieSearchDTO(movie, null));
        }

        // Genres removed, return empty page
        return Page.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryByName(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            return null;
        }
        String normalized = categoryName.trim().toLowerCase();

        LocalDate today = LocalDate.now();

        if (normalized.equals("featured")) {
            Page<Movie> page = movieRepository.findByIsFeaturedTrueAndIsAvailableTrueAndReleased(today, PageRequest.of(0, 12));
            List<MovieSearchDTO> movies = page.getContent().stream().map(m -> convertToMovieSearchDTO(m, null)).toList();
            return CategoryDTO.builder()
                    .name("featured")
                    .displayName("Featured")
                    .type("FEATURED")
                    .movies(movies)
                    .totalMovies(page.getTotalElements())
                    .build();
        }
        if (normalized.equals("trending")) {
            Page<Movie> page = movieRepository.findTrendingNowShowing(today, PageRequest.of(0, 12));
            List<MovieSearchDTO> movies = page.getContent().stream().map(m -> convertToMovieSearchDTO(m, null)).toList();
            return CategoryDTO.builder()
                    .name("trending")
                    .displayName("Trending")
                    .type("TRENDING")
                    .movies(movies)
                    .totalMovies(page.getTotalElements())
                    .build();
        }
        if (normalized.equals("new")) {
            Page<Movie> page = movieRepository.findByIsAvailableTrueAndReleased(today, PageRequest.of(0, 12, Sort.by("releaseDate").descending().and(Sort.by("createdAt").descending())));
            List<MovieSearchDTO> movies = page.getContent().stream().map(m -> convertToMovieSearchDTO(m, null)).toList();
            return CategoryDTO.builder()
                    .name("new")
                    .displayName("New Releases")
                    .type("NEW")
                    .movies(movies)
                    .totalMovies(page.getTotalElements())
                    .build();
        }

        return CategoryDTO.builder()
                .name(normalized)
                .displayName(categoryName)
                .type("GENRE")
                .movies(List.of())
                .totalMovies(0L)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieSearchDTO> getSimilarMovies(Long userId, Long movieId, int limit) {
        return List.of();
    }

    private String getStreamingUrl(Movie movie, String quality) {
        log.info("Getting streaming URL for movie {} with quality {}", movie.getId(), quality);
        
        String directUrl = normalizeStoredVideoPath(movie.getVideoUrl(), movie.getId());
        if (directUrl != null) {
            log.info("Using original video URL for movie {}: {}", movie.getId(), directUrl);
            return directUrl;
        }
        
        // Special handling for movie ID 19
        if (movie.getId() == 19) {
            String url = "/api/videos/stream/19/19_" + quality + ".mp4";
            log.info("Using hardcoded URL for movie 19 quality {}: {}", quality, url);
            return url;
        }
        
        // If HLS or pre-set streaming URL is configured, prefer it (only if it matches expected pattern)
        if (movie.getStreamingUrl() != null && !movie.getStreamingUrl().isEmpty()) {
            log.info("Using pre-set streaming URL: {}", movie.getStreamingUrl());
            return movie.getStreamingUrl();
        }

        // Try to find VideoResolution entity with the requested quality
        Optional<VideoResolution> videoResolution = videoResolutionRepository
                .findByMovieIdAndQualityAndIsAvailableTrue(movie.getId(), quality);
        
        if (videoResolution.isPresent()) {
            String url = videoResolution.get().getVideoUrl();
            log.info("Found VideoResolution for quality {}: {}", quality, url);
            return url;
        }

        // Try filesystem-based URL before falling back to null
        String expectedFile = movie.getId() + "_" + quality + ".mp4";
        Path expectedPath = Paths.get("uploads", "videos", String.valueOf(movie.getId()), expectedFile);
        if (Files.exists(expectedPath)) {
            String url = "/api/videos/stream/" + movie.getId() + "/" + expectedFile;
            log.info("Using filesystem-detected URL for quality {}: {}", quality, url);
            return url;
        }

        log.warn("No streaming URL found for movie {} with quality {}", movie.getId(), quality);
        return null;
    }

    private String getDownloadUrl(Movie movie, String quality) {
        Optional<VideoResolution> videoResolution = videoResolutionRepository
                .findByMovieIdAndQualityAndIsAvailableTrue(movie.getId(), quality);
        
        if (videoResolution.isPresent()) {
            return videoResolution.get().getVideoUrl();
        }

        return normalizeStoredVideoPath(movie.getVideoUrl(), movie.getId());
    }

    private String normalizeStoredVideoPath(String storedUrl, Long movieId) {
        if (storedUrl == null || storedUrl.isBlank()) {
            return null;
        }
        String normalized = storedUrl.replace("\\", "/").trim();
        if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
            return normalized;
        }
        if (normalized.startsWith("/api/")) {
            return normalized;
        }
        if (normalized.startsWith("uploads/")) {
            normalized = normalized.substring("uploads/".length());
        }
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.startsWith("videos/")) {
            normalized = normalized.substring("videos/".length());
        }
        if (normalized.isBlank()) {
            return null;
        }
        if (normalized.contains("/")) {
            return "/api/videos/stream/" + normalized;
        }
        if (movieId != null) {
            return "/api/videos/stream/" + movieId + "/" + normalized;
        }
        return "/api/videos/stream/" + normalized;
    }

    private List<String> resolveAvailableQualities(Movie movie) {
        List<String> qualities = new ArrayList<>(getAvailableQualitiesByMovieId(movie.getId()));
        if (!qualities.isEmpty()) {
            return qualities;
        }
        if (movie.getAvailableQualities() != null && !movie.getAvailableQualities().isEmpty()) {
            return new ArrayList<>(movie.getAvailableQualities());
        }
        if (movie.getVideoQuality() != null && !movie.getVideoQuality().isBlank()) {
            return new ArrayList<>(List.of(movie.getVideoQuality()));
        }
        return new ArrayList<>(List.of("original"));
    }

    private String resolveRequestedQuality(List<String> availableQualities, String requestedQuality) {
        if (requestedQuality != null && !requestedQuality.isBlank() && availableQualities.contains(requestedQuality)) {
            return requestedQuality;
        }
        if (!availableQualities.isEmpty()) {
            return availableQualities.get(0);
        }
        return "original";
    }

    private String resolveSubtitleLanguage(String requestedLanguage, List<SubtitleDTO> subtitles) {
        if (requestedLanguage != null && !requestedLanguage.isBlank()) {
            return requestedLanguage;
        }
        return subtitles.stream()
                .filter(SubtitleDTO::getIsDefault)
                .map(SubtitleDTO::getLanguageCode)
                .findFirst()
                .orElseGet(() -> subtitles.stream()
                        .map(SubtitleDTO::getLanguageCode)
                        .findFirst()
                        .orElse(null));
    }

    private String resolveSubtitleUrl(String languageCode, List<SubtitleDTO> subtitles) {
        if (languageCode == null || languageCode.isBlank()) {
            return null;
        }
        return subtitles.stream()
                .filter(dto -> dto.getLanguageCode().equalsIgnoreCase(languageCode))
                .map(SubtitleDTO::getSubtitleUrl)
                .findFirst()
                .orElse(null);
    }

    private UserProfileDTO convertToUserProfileDTO(User user) {
        return UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .birthday(user.getBirthday())
                .avatarUrl(user.getAvatarUrl())
                .preferredLanguage(user.getPreferredLanguage())
                .preferredQuality(user.getPreferredQuality())
                .autoPlay(user.getAutoPlay())
                .subtitleEnabled(user.getSubtitleEnabled())
                .emailVerified(user.getEmailVerified())
                .phoneVerified(user.getPhoneVerified())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private WatchHistoryDTO convertToWatchHistoryDTO(WatchHistory watchHistory) {
        return WatchHistoryDTO.builder()
                .id(watchHistory.getId())
                .movieId(watchHistory.getMovie().getId())
                .movieTitle(watchHistory.getMovie().getTitle())
                .moviePosterUrl(watchHistory.getMovie().getPosterUrl())
                .watchDurationSeconds(watchHistory.getWatchDurationSeconds())
                .totalDurationSeconds(watchHistory.getTotalDurationSeconds())
                .watchPercentage(watchHistory.getWatchPercentage())
                .lastWatchedAt(watchHistory.getLastWatchedAt())
                .isCompleted(watchHistory.getIsCompleted())
                .deviceType(watchHistory.getDeviceType())
                .quality(watchHistory.getQuality())
                .subtitleLanguage(watchHistory.getSubtitleLanguage())
                .build();
    }

    private FavoriteDTO convertToFavoriteDTO(Favorite favorite) {
        return FavoriteDTO.builder()
                .id(favorite.getId())
                .movieId(favorite.getMovie().getId())
                .movieTitle(favorite.getMovie().getTitle())
                .moviePosterUrl(favorite.getMovie().getPosterUrl())
                .movieSynopsis(favorite.getMovie().getSynopsis())
                .movieYear(favorite.getMovie().getYear())
                .movieRating(favorite.getMovie().getAverageRating())
                .addedAt(favorite.getAddedAt())
                .build();
    }

    private WatchlistDTO convertToWatchlistDTO(Watchlist watchlist) {
        return WatchlistDTO.builder()
                .id(watchlist.getId())
                .movieId(watchlist.getMovie().getId())
                .movieTitle(watchlist.getMovie().getTitle())
                .moviePosterUrl(watchlist.getMovie().getPosterUrl())
                .movieSynopsis(watchlist.getMovie().getSynopsis())
                .movieYear(watchlist.getMovie().getYear())
                .movieRating(watchlist.getMovie().getAverageRating())
                .priority(watchlist.getPriority())
                .notes(watchlist.getNotes())
                .addedAt(watchlist.getAddedAt())
                .build();
    }

    private SubtitleDTO convertToSubtitleDTO(Subtitle subtitle) {
        String subtitleUrl = subtitle.getSubtitleUrl();
        String format = null;
        if (subtitleUrl != null && subtitleUrl.contains(".")) {
            format = subtitleUrl.substring(subtitleUrl.lastIndexOf(".") + 1).toLowerCase();
        }
        return SubtitleDTO.builder()
                .id(subtitle.getId())
                .language(subtitle.getLanguageName())
                .languageCode(subtitle.getLanguageCode())
                .languageName(subtitle.getLanguageName())
                .url(subtitleUrl)
                .subtitleUrl(subtitleUrl)
                .format(format)
                .isDefault(subtitle.getIsDefault())
                .isAutoGenerated(subtitle.getIsAutoGenerated())
                .encoding(subtitle.getEncoding())
                .fileSizeBytes(subtitle.getFileSizeBytes())
                .build();
    }

    private MovieSearchDTO convertToMovieSearchDTO(Movie movie, Long userId) {
        return MovieSearchDTO.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .synopsis(movie.getSynopsis())
                .year(movie.getYear())
                .actors(movie.getActors())
                .directorName(movie.getDirector() != null ? movie.getDirector().getName() : null)
                .country(movie.getCountry() != null ? movie.getCountry().getName() : null)
                .language(movie.getLanguage())
                .ageRating(movie.getAgeRating())
                .imdbRating(movie.getImdbRating())
                .averageRating(movie.getAverageRating())
                .viewCount(movie.getViewCount())
                .posterUrl(movie.getPosterUrl())
                .thumbnailUrl(movie.getThumbnailUrl())
                .trailerUrl(movie.getTrailerUrl())
                .isFeatured(movie.getIsFeatured())
                .isTrending(movie.getIsTrending())
                .releaseDate(movie.getReleaseDate())
                .isInWatchlist(isInWatchlist(userId, movie.getId()))
                .isFavorite(isFavorite(userId, movie.getId()))
                .build();
    }

    // Video Resolution Management
    @Override
    @Transactional(readOnly = true)
    public List<String> getAvailableQualitiesByMovieId(Long movieId) {
        return videoResolutionService.getAvailableQualitiesByMovieId(movieId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object> getAvailableResolutionsByMovieId(Long movieId) {
        List<AvailableResolutionResponse> resolutions = videoResolutionService.getAvailableResolutionsByMovieId(movieId);
        return resolutions.stream()
                .map(resolution -> (Object) resolution)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public String getBestAvailableQuality(Long movieId, String preferredQuality) {
        return videoResolutionService.getBestAvailableQuality(movieId, preferredQuality);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isResolutionAvailable(Long movieId, String quality) {
        return videoResolutionService.isResolutionAvailable(movieId, quality);
    }

    @Override
    public ResolutionChangeResponse changeVideoResolution(ResolutionChangeRequest request) {
        return videoResolutionService.changeVideoResolution(request);
    }

    private WatchlistCollection getOrCreateDefaultWatchlistCollection(User user) {
        // Try to find existing default collection
        return watchlistCollectionRepository.findByUserAndIsDefaultTrue(user)
                .orElseGet(() -> {
                    // Create default collection if it doesn't exist
                    WatchlistCollection defaultCollection = WatchlistCollection.builder()
                            .name("My Watchlist")
                            .description("Default watchlist")
                            .user(user)
                            .isDefault(true)
                            .isPublic(false)
                            .movieCount(0)
                            .build();
                    return watchlistCollectionRepository.save(defaultCollection);
                });
    }

    @Scheduled(cron = "0 0 0 * * ?") // Chạy hàng ngày vào lúc nửa đêm
    public void updateMovieStatuses() {
        List<Movie> movies = movieRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Movie movie : movies) {
            if (movie.getReleaseDate() != null) {
                if (movie.getReleaseDate().isBefore(today) || movie.getReleaseDate().isEqual(today)) {
                    movie.setStatus("NOW_SHOWING");
                } else {
                    movie.setStatus("UPCOMING");
                }
            }
        }

        movieRepository.saveAll(movies);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieSearchDTO> getNewMovies(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("releaseDate").descending().and(Sort.by("createdAt").descending()));
        return movieRepository.findByIsAvailableTrueAndReleased(LocalDate.now(), pageable)
                .getContent()
                .stream()
                .map(movie -> convertToMovieSearchDTO(movie, userId))
                .collect(Collectors.toList());
    }
}
