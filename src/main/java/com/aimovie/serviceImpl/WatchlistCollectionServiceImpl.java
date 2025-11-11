package com.aimovie.serviceImpl;

import com.aimovie.dto.WatchlistDTOs.*;
import com.aimovie.entity.Movie;
import com.aimovie.entity.User;
import com.aimovie.entity.Watchlist;
import com.aimovie.entity.WatchlistCollection;
import com.aimovie.repository.MovieRepository;
import com.aimovie.repository.UserRepository;
import com.aimovie.repository.WatchlistCollectionRepository;
import com.aimovie.repository.WatchlistRepository;
import com.aimovie.service.WatchlistCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WatchlistCollectionServiceImpl implements WatchlistCollectionService {

    private final WatchlistCollectionRepository watchlistCollectionRepository;
    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    @Override
    public WatchlistCollectionResponse createWatchlistCollection(Long userId, WatchlistCollectionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (watchlistCollectionRepository.existsByUserAndName(user, request.getName())) {
            throw new RuntimeException("Watchlist with name '" + request.getName() + "' already exists");
        }

        WatchlistCollection watchlistCollection = WatchlistCollection.builder()
                .name(request.getName())
                .description(request.getDescription())
                .user(user)
                .isPublic(request.getIsPublic())
                .colorCode(request.getColorCode())
                .icon(request.getIcon())
                .movieCount(0)
                .isDefault(false)
                .build();

        WatchlistCollection savedCollection = watchlistCollectionRepository.save(watchlistCollection);
        log.info("Created watchlist collection: {} for user: {}", request.getName(), userId);

        return mapToWatchlistCollectionResponse(savedCollection);
    }

    @Override
    public WatchlistCollectionResponse updateWatchlistCollection(Long userId, Long collectionId, WatchlistCollectionUpdateRequest request) {
        WatchlistCollection watchlistCollection = watchlistCollectionRepository.findByUserIdAndId(userId, collectionId)
                .orElseThrow(() -> new RuntimeException("Watchlist collection not found"));

        if (request.getName() != null && !request.getName().equals(watchlistCollection.getName())) {
            if (watchlistCollectionRepository.existsByUserAndName(watchlistCollection.getUser(), request.getName())) {
                throw new RuntimeException("Watchlist with name '" + request.getName() + "' already exists");
            }
            watchlistCollection.setName(request.getName());
        }

        if (request.getDescription() != null) {
            watchlistCollection.setDescription(request.getDescription());
        }
        if (request.getIsPublic() != null) {
            watchlistCollection.setIsPublic(request.getIsPublic());
        }
        if (request.getColorCode() != null) {
            watchlistCollection.setColorCode(request.getColorCode());
        }
        if (request.getIcon() != null) {
            watchlistCollection.setIcon(request.getIcon());
        }

        WatchlistCollection updatedCollection = watchlistCollectionRepository.save(watchlistCollection);
        log.info("Updated watchlist collection: {} for user: {}", updatedCollection.getName(), userId);

        return mapToWatchlistCollectionResponse(updatedCollection);
    }

    @Override
    @Transactional(readOnly = true)
    public WatchlistCollectionResponse getWatchlistCollectionById(Long userId, Long collectionId) {
        WatchlistCollection watchlistCollection = watchlistCollectionRepository.findByUserIdAndId(userId, collectionId)
                .orElseThrow(() -> new RuntimeException("Watchlist collection not found"));

        return mapToWatchlistCollectionResponse(watchlistCollection);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WatchlistCollectionResponse> getUserWatchlistCollections(Long userId) {
        List<WatchlistCollection> collections = watchlistCollectionRepository.findByUserIdOrderByDefaultAndUpdatedAt(userId);
        return collections.stream()
                .map(this::mapToWatchlistCollectionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WatchlistSummaryResponse> getUserWatchlistSummaries(Long userId) {
        List<WatchlistCollection> collections = watchlistCollectionRepository.findByUserIdOrderByDefaultAndUpdatedAt(userId);
        return collections.stream()
                .map(this::mapToWatchlistSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserWatchlistOverviewResponse getUserWatchlistOverview(Long userId) {
        List<WatchlistCollection> collections = watchlistCollectionRepository.findByUserIdOrderByDefaultAndUpdatedAt(userId);
        
        List<WatchlistSummaryResponse> summaries = collections.stream()
                .map(this::mapToWatchlistSummaryResponse)
                .collect(Collectors.toList());

        WatchlistSummaryResponse defaultWatchlist = collections.stream()
                .filter(WatchlistCollection::getIsDefault)
                .findFirst()
                .map(this::mapToWatchlistSummaryResponse)
                .orElse(null);

        int totalMovies = collections.stream()
                .mapToInt(WatchlistCollection::getMovieCount)
                .sum();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserWatchlistOverviewResponse.builder()
                .userId(userId)
                .userName(user.getUsername())
                .watchlists(summaries)
                .totalWatchlists(collections.size())
                .totalMovies(totalMovies)
                .defaultWatchlist(defaultWatchlist)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public WatchlistCollectionResponse getDefaultWatchlistCollection(Long userId) {
        WatchlistCollection defaultCollection = watchlistCollectionRepository.findDefaultByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Default watchlist collection not found"));

        return mapToWatchlistCollectionResponse(defaultCollection);
    }

    @Override
    public WatchlistCollectionResponse setDefaultWatchlistCollection(Long userId, Long collectionId) {
        // Remove default flag from current default collection
        watchlistCollectionRepository.findDefaultByUserId(userId)
                .ifPresent(collection -> {
                    collection.setIsDefault(false);
                    watchlistCollectionRepository.save(collection);
                });

        // Set new default collection
        WatchlistCollection newDefaultCollection = watchlistCollectionRepository.findByUserIdAndId(userId, collectionId)
                .orElseThrow(() -> new RuntimeException("Watchlist collection not found"));

        newDefaultCollection.setIsDefault(true);
        WatchlistCollection savedCollection = watchlistCollectionRepository.save(newDefaultCollection);
        
        log.info("Set default watchlist collection: {} for user: {}", savedCollection.getName(), userId);
        return mapToWatchlistCollectionResponse(savedCollection);
    }

    @Override
    public void deleteWatchlistCollection(Long userId, Long collectionId) {
        WatchlistCollection watchlistCollection = watchlistCollectionRepository.findByUserIdAndId(userId, collectionId)
                .orElseThrow(() -> new RuntimeException("Watchlist collection not found"));

        if (watchlistCollection.getIsDefault()) {
            throw new RuntimeException("Cannot delete default watchlist collection");
        }

        // Delete all watchlist items in this collection
        watchlistRepository.deleteByWatchlistCollection(watchlistCollection);
        
        // Delete the collection
        watchlistCollectionRepository.delete(watchlistCollection);
        
        log.info("Deleted watchlist collection: {} for user: {}", watchlistCollection.getName(), userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WatchlistCollectionResponse> getPublicWatchlistCollections() {
        List<WatchlistCollection> publicCollections = watchlistCollectionRepository.findPublicWatchlists();
        return publicCollections.stream()
                .map(this::mapToWatchlistCollectionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WatchlistCollectionResponse> searchWatchlistCollections(Long userId, String query) {
        List<WatchlistCollection> collections = watchlistCollectionRepository.findByUserIdAndNameContaining(userId, query);
        return collections.stream()
                .map(this::mapToWatchlistCollectionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isWatchlistCollectionNameAvailable(Long userId, String name) {
        return !watchlistCollectionRepository.existsByUserIdAndName(userId, name);
    }

    @Override
    public WatchlistCollectionResponse duplicateWatchlistCollection(Long userId, Long sourceCollectionId, String newName) {
        WatchlistCollection sourceCollection = watchlistCollectionRepository.findByUserIdAndId(userId, sourceCollectionId)
                .orElseThrow(() -> new RuntimeException("Source watchlist collection not found"));

        if (watchlistCollectionRepository.existsByUserIdAndName(userId, newName)) {
            throw new RuntimeException("Watchlist with name '" + newName + "' already exists");
        }

        // Create new collection
        WatchlistCollection newCollection = WatchlistCollection.builder()
                .name(newName)
                .description("Copy of " + sourceCollection.getName())
                .user(sourceCollection.getUser())
                .isPublic(false)
                .colorCode(sourceCollection.getColorCode())
                .icon(sourceCollection.getIcon())
                .movieCount(0)
                .isDefault(false)
                .build();

        WatchlistCollection savedCollection = watchlistCollectionRepository.save(newCollection);

        // Copy watchlist items
        List<Watchlist> sourceItems = watchlistRepository.findByWatchlistCollectionOrderByPriorityDescAddedAtDesc(sourceCollection);
        for (Watchlist sourceItem : sourceItems) {
            Watchlist newItem = Watchlist.builder()
                    .watchlistCollection(savedCollection)
                    .movie(sourceItem.getMovie())
                    .notes(sourceItem.getNotes())
                    .priority(sourceItem.getPriority())
                    .addedAt(LocalDateTime.now())
                    .build();
            watchlistRepository.save(newItem);
        }

        // Update movie count
        savedCollection.setMovieCount(sourceItems.size());
        WatchlistCollection updatedCollection = watchlistCollectionRepository.save(savedCollection);

        log.info("Duplicated watchlist collection: {} to {} for user: {}", sourceCollection.getName(), newName, userId);
        return mapToWatchlistCollectionResponse(updatedCollection);
    }

    @Override
    public WatchlistCollectionResponse mergeWatchlistCollections(Long userId, Long targetCollectionId, List<Long> sourceCollectionIds) {
        WatchlistCollection targetCollection = watchlistCollectionRepository.findByUserIdAndId(userId, targetCollectionId)
                .orElseThrow(() -> new RuntimeException("Target watchlist collection not found"));

        int totalMergedMovies = 0;

        for (Long sourceCollectionId : sourceCollectionIds) {
            WatchlistCollection sourceCollection = watchlistCollectionRepository.findByUserIdAndId(userId, sourceCollectionId)
                    .orElseThrow(() -> new RuntimeException("Source watchlist collection not found"));

            List<Watchlist> sourceItems = watchlistRepository.findByWatchlistCollectionOrderByPriorityDescAddedAtDesc(sourceCollection);
            
            for (Watchlist sourceItem : sourceItems) {
                // Check if movie already exists in target collection
                if (!watchlistRepository.existsByWatchlistCollectionAndMovie(targetCollection, sourceItem.getMovie())) {
                    Watchlist newItem = Watchlist.builder()
                            .watchlistCollection(targetCollection)
                            .movie(sourceItem.getMovie())
                            .notes(sourceItem.getNotes())
                            .priority(sourceItem.getPriority())
                            .addedAt(LocalDateTime.now())
                            .build();
                    watchlistRepository.save(newItem);
                    totalMergedMovies++;
                }
            }

            // Delete source collection if it's not the target
            if (!sourceCollectionId.equals(targetCollectionId)) {
                watchlistRepository.deleteByWatchlistCollection(sourceCollection);
                watchlistCollectionRepository.delete(sourceCollection);
            }
        }

        // Update movie count
        targetCollection.setMovieCount((int) watchlistRepository.countByWatchlistCollection(targetCollection));
        WatchlistCollection updatedCollection = watchlistCollectionRepository.save(targetCollection);

        log.info("Merged {} movies into watchlist collection: {} for user: {}", totalMergedMovies, targetCollection.getName(), userId);
        return mapToWatchlistCollectionResponse(updatedCollection);
    }

    // Watchlist Item Management Methods
    @Override
    public WatchlistItemResponse addMovieToWatchlist(Long userId, WatchlistAddRequest request) {
        WatchlistCollection watchlistCollection = watchlistCollectionRepository.findByUserIdAndId(userId, request.getWatchlistCollectionId())
                .orElseThrow(() -> new RuntimeException("Watchlist collection not found"));

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + request.getMovieId()));

        if (watchlistRepository.existsByWatchlistCollectionAndMovie(watchlistCollection, movie)) {
            throw new RuntimeException("Movie already exists in this watchlist");
        }

        Watchlist watchlistItem = Watchlist.builder()
                .watchlistCollection(watchlistCollection)
                .movie(movie)
                .notes(request.getNotes())
                .priority(request.getPriority())
                .addedAt(LocalDateTime.now())
                .build();

        Watchlist savedItem = watchlistRepository.save(watchlistItem);

        // Update movie count
        watchlistCollection.setMovieCount((int) watchlistRepository.countByWatchlistCollection(watchlistCollection));
        watchlistCollectionRepository.save(watchlistCollection);

        log.info("Added movie: {} to watchlist: {} for user: {}", movie.getTitle(), watchlistCollection.getName(), userId);
        return mapToWatchlistItemResponse(savedItem);
    }

    @Override
    public void removeMovieFromWatchlist(Long userId, Long collectionId, Long movieId) {
        WatchlistCollection watchlistCollection = watchlistCollectionRepository.findByUserIdAndId(userId, collectionId)
                .orElseThrow(() -> new RuntimeException("Watchlist collection not found"));

        Watchlist watchlistItem = watchlistRepository.findByWatchlistCollectionAndMovie(watchlistCollection, 
                movieRepository.findById(movieId).orElseThrow(() -> new RuntimeException("Movie not found")))
                .orElseThrow(() -> new RuntimeException("Movie not found in this watchlist"));

        watchlistRepository.delete(watchlistItem);

        // Update movie count
        watchlistCollection.setMovieCount((int) watchlistRepository.countByWatchlistCollection(watchlistCollection));
        watchlistCollectionRepository.save(watchlistCollection);

        log.info("Removed movie: {} from watchlist: {} for user: {}", movieId, watchlistCollection.getName(), userId);
    }

    @Override
    public WatchlistItemResponse updateWatchlistItem(Long userId, Long collectionId, Long movieId, WatchlistItemUpdateRequest request) {
        WatchlistCollection watchlistCollection = watchlistCollectionRepository.findByUserIdAndId(userId, collectionId)
                .orElseThrow(() -> new RuntimeException("Watchlist collection not found"));

        Watchlist watchlistItem = watchlistRepository.findByWatchlistCollectionAndMovie(watchlistCollection,
                movieRepository.findById(movieId).orElseThrow(() -> new RuntimeException("Movie not found")))
                .orElseThrow(() -> new RuntimeException("Movie not found in this watchlist"));

        if (request.getNotes() != null) {
            watchlistItem.setNotes(request.getNotes());
        }
        if (request.getPriority() != null) {
            watchlistItem.setPriority(request.getPriority());
        }

        Watchlist updatedItem = watchlistRepository.save(watchlistItem);
        log.info("Updated watchlist item for movie: {} in watchlist: {} for user: {}", movieId, watchlistCollection.getName(), userId);

        return mapToWatchlistItemResponse(updatedItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WatchlistItemResponse> getWatchlistItems(Long userId, Long collectionId) {
        WatchlistCollection watchlistCollection = watchlistCollectionRepository.findByUserIdAndId(userId, collectionId)
                .orElseThrow(() -> new RuntimeException("Watchlist collection not found"));

        List<Watchlist> watchlistItems = watchlistRepository.findByWatchlistCollectionOrderByPriorityDescAddedAtDesc(watchlistCollection);
        return watchlistItems.stream()
                .map(this::mapToWatchlistItemResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WatchlistItemResponse> getWatchlistItemsPaginated(Long userId, Long collectionId, Pageable pageable) {
        WatchlistCollection watchlistCollection = watchlistCollectionRepository.findByUserIdAndId(userId, collectionId)
                .orElseThrow(() -> new RuntimeException("Watchlist collection not found"));

        Page<Watchlist> watchlistItems = watchlistRepository.findByWatchlistCollectionOrderByPriorityDescAddedAtDesc(watchlistCollection, pageable);
        return watchlistItems.map(this::mapToWatchlistItemResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMovieInWatchlist(Long userId, Long collectionId, Long movieId) {
        WatchlistCollection watchlistCollection = watchlistCollectionRepository.findByUserIdAndId(userId, collectionId)
                .orElseThrow(() -> new RuntimeException("Watchlist collection not found"));

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        return watchlistRepository.existsByWatchlistCollectionAndMovie(watchlistCollection, movie);
    }

    @Override
    public void moveMovieBetweenWatchlists(Long userId, Long fromCollectionId, Long toCollectionId, Long movieId) {
        WatchlistCollection fromCollection = watchlistCollectionRepository.findByUserIdAndId(userId, fromCollectionId)
                .orElseThrow(() -> new RuntimeException("Source watchlist collection not found"));

        WatchlistCollection toCollection = watchlistCollectionRepository.findByUserIdAndId(userId, toCollectionId)
                .orElseThrow(() -> new RuntimeException("Target watchlist collection not found"));

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        Watchlist watchlistItem = watchlistRepository.findByWatchlistCollectionAndMovie(fromCollection, movie)
                .orElseThrow(() -> new RuntimeException("Movie not found in source watchlist"));

        if (watchlistRepository.existsByWatchlistCollectionAndMovie(toCollection, movie)) {
            throw new RuntimeException("Movie already exists in target watchlist");
        }

        // Move the item
        watchlistItem.setWatchlistCollection(toCollection);
        watchlistRepository.save(watchlistItem);

        // Update movie counts
        fromCollection.setMovieCount((int) watchlistRepository.countByWatchlistCollection(fromCollection));
        toCollection.setMovieCount((int) watchlistRepository.countByWatchlistCollection(toCollection));
        watchlistCollectionRepository.save(fromCollection);
        watchlistCollectionRepository.save(toCollection);

        log.info("Moved movie: {} from watchlist: {} to watchlist: {} for user: {}", 
                movieId, fromCollection.getName(), toCollection.getName(), userId);
    }

    @Override
    public void reorderWatchlistItems(Long userId, Long collectionId, List<Long> movieIds) {
        WatchlistCollection watchlistCollection = watchlistCollectionRepository.findByUserIdAndId(userId, collectionId)
                .orElseThrow(() -> new RuntimeException("Watchlist collection not found"));

        for (int i = 0; i < movieIds.size(); i++) {
            Long movieId = movieIds.get(i);
            Watchlist watchlistItem = watchlistRepository.findByWatchlistCollectionAndMovie(watchlistCollection,
                    movieRepository.findById(movieId).orElseThrow(() -> new RuntimeException("Movie not found")))
                    .orElseThrow(() -> new RuntimeException("Movie not found in watchlist"));

            watchlistItem.setPriority(movieIds.size() - i); // Higher priority for items earlier in the list
            watchlistRepository.save(watchlistItem);
        }

        log.info("Reordered watchlist items in collection: {} for user: {}", watchlistCollection.getName(), userId);
    }

    // Mapping methods
    private WatchlistCollectionResponse mapToWatchlistCollectionResponse(WatchlistCollection collection) {
        List<WatchlistItemResponse> movies = collection.getWatchlistItems().stream()
                .map(this::mapToWatchlistItemResponse)
                .collect(Collectors.toList());

        return WatchlistCollectionResponse.builder()
                .id(collection.getId())
                .name(collection.getName())
                .description(collection.getDescription())
                .userId(collection.getUser().getId())
                .userName(collection.getUser().getUsername())
                .isPublic(collection.getIsPublic())
                .isDefault(collection.getIsDefault())
                .movieCount(collection.getMovieCount())
                .colorCode(collection.getColorCode())
                .icon(collection.getIcon())
                .createdAt(collection.getCreatedAt())
                .updatedAt(collection.getUpdatedAt())
                .movies(movies)
                .build();
    }

    private WatchlistSummaryResponse mapToWatchlistSummaryResponse(WatchlistCollection collection) {
        return WatchlistSummaryResponse.builder()
                .id(collection.getId())
                .name(collection.getName())
                .description(collection.getDescription())
                .movieCount(collection.getMovieCount())
                .colorCode(collection.getColorCode())
                .icon(collection.getIcon())
                .isDefault(collection.getIsDefault())
                .lastUpdated(collection.getUpdatedAt())
                .build();
    }

    private WatchlistItemResponse mapToWatchlistItemResponse(Watchlist watchlist) {
        return WatchlistItemResponse.builder()
                .id(watchlist.getId())
                .movieId(watchlist.getMovie().getId())
                .movieTitle(watchlist.getMovie().getTitle())
                .moviePosterUrl(watchlist.getMovie().getPosterUrl())
                .movieSynopsis(watchlist.getMovie().getSynopsis())
                .movieYear(watchlist.getMovie().getYear())
                .movieRating(watchlist.getMovie().getAverageRating())
                .notes(watchlist.getNotes())
                .priority(watchlist.getPriority())
                .addedAt(watchlist.getAddedAt())
                .createdAt(watchlist.getCreatedAt())
                .build();
    }
}
