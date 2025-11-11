package com.aimovie.service;

import com.aimovie.dto.WatchlistDTO;
import com.aimovie.dto.WatchlistCollectionDTO;
import com.aimovie.entity.Movie;
import com.aimovie.entity.User;
import com.aimovie.entity.Watchlist;
import com.aimovie.entity.WatchlistCollection;
import com.aimovie.repository.MovieRepository;
import com.aimovie.repository.UserRepository;
import com.aimovie.repository.WatchlistRepository;
import com.aimovie.repository.WatchlistCollectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final WatchlistCollectionRepository watchlistCollectionRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    public WatchlistDTO addToWatchlist(Long userId, Long movieId) {
        return addToWatchlist(userId, movieId, null, null, null);
    }

    public WatchlistDTO addToWatchlist(Long userId, Long movieId, String notes) {
        return addToWatchlist(userId, movieId, notes, null, null);
    }

    public WatchlistDTO addToWatchlist(Long userId, Long movieId, String notes, Integer priority, Long collectionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        // Get watchlist collection (use provided collectionId or default)
        WatchlistCollection collection;
        if (collectionId != null) {
            collection = watchlistCollectionRepository.findById(collectionId)
                    .orElseThrow(() -> new RuntimeException("Watchlist collection not found"));
            // Verify collection belongs to user
            if (!collection.getUser().getId().equals(userId)) {
                throw new RuntimeException("Access denied to watchlist collection");
            }
        } else {
            collection = getOrCreateDefaultWatchlistCollection(user);
        }

        if (watchlistRepository.existsByWatchlistCollectionAndMovie(collection, movie)) {
            throw new RuntimeException("Movie already in watchlist");
        }

        Watchlist watchlist = Watchlist.builder()
                .user(user)
                .watchlistCollection(collection)
                .movie(movie)
                .isInWatchlist(true)
                .priority(priority != null ? priority : 1)
                .notes(notes)
                .addedAt(java.time.LocalDateTime.now())
                .build();

        Watchlist savedItem = watchlistRepository.save(watchlist);
        return toResponseDTO(savedItem);
    }

    public List<WatchlistDTO> getUserWatchlist(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        WatchlistCollection defaultCollection = getOrCreateDefaultWatchlistCollection(user);
        List<Watchlist> watchlistItems = watchlistRepository.findByWatchlistCollectionOrderByPriorityDescAddedAtDesc(defaultCollection);
        return watchlistItems.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public void removeFromWatchlist(Long userId, Long movieId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        
        WatchlistCollection defaultCollection = getOrCreateDefaultWatchlistCollection(user);
        
        if (!watchlistRepository.existsByWatchlistCollectionAndMovie(defaultCollection, movie)) {
            throw new RuntimeException("Movie not in watchlist");
        }
        
        watchlistRepository.findByWatchlistCollectionAndMovie(defaultCollection, movie)
                .ifPresent(watchlistRepository::delete);
    }

    public boolean isInWatchlist(Long userId, Long movieId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        
        WatchlistCollection defaultCollection = getOrCreateDefaultWatchlistCollection(user);
        return watchlistRepository.existsByWatchlistCollectionAndMovie(defaultCollection, movie);
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

    private WatchlistDTO toResponseDTO(Watchlist item) {
        return WatchlistDTO.builder()
                .id(item.getId())
                .movieId(item.getMovie().getId())
                .movieTitle(item.getMovie().getTitle())
                .moviePosterUrl(item.getMovie().getPosterUrl())
                .movieYear(item.getMovie().getYear())
                .movieRating(item.getMovie().getAverageRating())
                .movieSynopsis(item.getMovie().getSynopsis())
                .priority(item.getPriority())
                .notes(item.getNotes())
                .addedAt(item.getAddedAt())
                .build();
    }

    // WatchlistCollection Management Methods
    public WatchlistCollectionDTO createWatchlistCollection(Long userId, String name, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        WatchlistCollection collection = WatchlistCollection.builder()
                .user(user)
                .name(name)
                .description(description)
                .isDefault(false)
                .isPublic(false)
                .movieCount(0)
                .build();

        WatchlistCollection savedCollection = watchlistCollectionRepository.save(collection);
        return toCollectionResponseDTO(savedCollection);
    }

    public List<WatchlistCollectionDTO> getUserWatchlistCollections(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<WatchlistCollection> collections = watchlistCollectionRepository.findByUser(user);
        return collections.stream()
                .map(this::toCollectionResponseDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    public WatchlistCollectionDTO getWatchlistCollection(Long userId, Long collectionId) {
        WatchlistCollection collection = watchlistCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Watchlist collection not found"));

        if (!collection.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied to watchlist collection");
        }

        return toCollectionResponseDTO(collection);
    }

    public List<WatchlistDTO> getWatchlistCollectionMovies(Long userId, Long collectionId) {
        WatchlistCollection collection = watchlistCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Watchlist collection not found"));

        if (!collection.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied to watchlist collection");
        }

        List<Watchlist> watchlists = watchlistRepository.findByWatchlistCollection(collection);
        return watchlists.stream()
                .map(this::toResponseDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    public WatchlistCollectionDTO updateWatchlistCollection(Long userId, Long collectionId, String name, String description) {
        WatchlistCollection collection = watchlistCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Watchlist collection not found"));

        if (!collection.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied to watchlist collection");
        }

        if (name != null && !name.trim().isEmpty()) {
            collection.setName(name);
        }
        if (description != null) {
            collection.setDescription(description);
        }

        WatchlistCollection savedCollection = watchlistCollectionRepository.save(collection);
        return toCollectionResponseDTO(savedCollection);
    }

    public void deleteWatchlistCollection(Long userId, Long collectionId) {
        WatchlistCollection collection = watchlistCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Watchlist collection not found"));

        if (!collection.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied to watchlist collection");
        }

        if (collection.getIsDefault()) {
            throw new RuntimeException("Cannot delete default watchlist collection");
        }

        // Delete all watchlist items in this collection
        List<Watchlist> watchlists = watchlistRepository.findByWatchlistCollection(collection);
        watchlistRepository.deleteAll(watchlists);

        // Delete the collection
        watchlistCollectionRepository.delete(collection);
    }

    private WatchlistCollectionDTO toCollectionResponseDTO(WatchlistCollection collection) {
        List<Watchlist> watchlists = watchlistRepository.findByWatchlistCollection(collection);
        
        return WatchlistCollectionDTO.builder()
                .id(collection.getId())
                .name(collection.getName())
                .description(collection.getDescription())
                .userId(collection.getUser().getId())
                .username(collection.getUser().getUsername())
                .movieCount(watchlists.size())
                .createdAt(collection.getCreatedAt())
                .updatedAt(collection.getUpdatedAt())
                .movies(watchlists.stream()
                        .map(this::toResponseDTO)
                        .collect(java.util.stream.Collectors.toList()))
                .build();
    }
}
