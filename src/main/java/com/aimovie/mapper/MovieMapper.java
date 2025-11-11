package com.aimovie.mapper;

import com.aimovie.dto.MovieDTOs;
import com.aimovie.entity.Category;
import com.aimovie.entity.Country;
import com.aimovie.entity.Director;
import com.aimovie.entity.Movie;
import com.aimovie.repository.CountryRepository;
import com.aimovie.repository.DirectorRepository;
import com.aimovie.service.VideoResolutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MovieMapper {

    private static CountryRepository countryRepository;
    private static DirectorRepository directorRepository;
    private static VideoResolutionService videoResolutionService;

    @Autowired
    public void setCountryRepository(CountryRepository countryRepository) {
        MovieMapper.countryRepository = countryRepository;
    }
    
    @Autowired
    public void setDirectorRepository(DirectorRepository directorRepository) {
        MovieMapper.directorRepository = directorRepository;
    }

    @Autowired
    public void setVideoResolutionService(VideoResolutionService videoResolutionService) {
        MovieMapper.videoResolutionService = videoResolutionService;
    }

    public static Movie toEntity(MovieDTOs.MovieCreateDTO dto) {
        if (dto == null) return null;
        return Movie.builder()
                .title(dto.getTitle())
                .synopsis(dto.getSynopsis())
                .year(dto.getYear())
                
                .posterUrl(dto.getPosterUrl())
                .thumbnailUrl(dto.getThumbnailUrl())
                .videoUrl(dto.getVideoUrl())
                .videoFormat(dto.getVideoFormat())
                .videoDuration(dto.getVideoDuration())
                .videoQuality(dto.getVideoQuality())
                .fileSizeBytes(dto.getFileSizeBytes())
                .streamingUrl(dto.getStreamingUrl())
                .isAvailable(dto.getIsAvailable())
                // Additional movie details
                .actors(dto.getActors() != null ? new java.util.ArrayList<>(dto.getActors()) : null)
                .director(findDirectorByName(dto.getDirectorName()))
                .country(findCountryByName(dto.getCountry()))
                .language(dto.getLanguage())
                .ageRating(dto.getAgeRating())
                .imdbRating(dto.getImdbRating())
                .viewCount(dto.getViewCount())
                .likeCount(dto.getLikeCount())
                .dislikeCount(dto.getDislikeCount())
                .averageRating(dto.getAverageRating())
                .totalRatings(dto.getTotalRatings())
                .commentCount(dto.getCommentCount())
                .isFeatured(dto.getIsFeatured())
                .isTrending(dto.getIsTrending())
                .releaseDate(dto.getReleaseDate())
                .trailerUrl(dto.getTrailerUrl())
                .downloadEnabled(dto.getDownloadEnabled())
                .maxDownloadQuality(dto.getMaxDownloadQuality())
                .availableQualities(dto.getAvailableQualities() != null ? new java.util.ArrayList<>(dto.getAvailableQualities()) : null)
                .build();
    }

    public static void updateEntity(Movie entity, MovieDTOs.MovieUpdateDTO dto) {
        if (entity == null || dto == null) return;
        if (dto.getTitle() != null) entity.setTitle(dto.getTitle());
        if (dto.getSynopsis() != null) entity.setSynopsis(dto.getSynopsis());
        if (dto.getYear() != null) entity.setYear(dto.getYear());
        
        if (dto.getPosterUrl() != null) entity.setPosterUrl(dto.getPosterUrl());
        if (dto.getThumbnailUrl() != null) entity.setThumbnailUrl(dto.getThumbnailUrl());
        if (dto.getVideoUrl() != null) entity.setVideoUrl(dto.getVideoUrl());
        if (dto.getVideoFormat() != null) entity.setVideoFormat(dto.getVideoFormat());
        if (dto.getVideoDuration() != null) entity.setVideoDuration(dto.getVideoDuration());
        if (dto.getVideoQuality() != null) entity.setVideoQuality(dto.getVideoQuality());
        if (dto.getFileSizeBytes() != null) entity.setFileSizeBytes(dto.getFileSizeBytes());
        if (dto.getStreamingUrl() != null) entity.setStreamingUrl(dto.getStreamingUrl());
        if (dto.getIsAvailable() != null) entity.setIsAvailable(dto.getIsAvailable());
        // Additional movie details
        if (dto.getActors() != null) entity.setActors(new java.util.ArrayList<>(dto.getActors()));
        if (dto.getDirectorName() != null) entity.setDirector(findDirectorByName(dto.getDirectorName()));
        if (dto.getCountry() != null) entity.setCountry(findCountryByName(dto.getCountry()));
        if (dto.getLanguage() != null) entity.setLanguage(dto.getLanguage());
        if (dto.getAgeRating() != null) entity.setAgeRating(dto.getAgeRating());
        if (dto.getImdbRating() != null) entity.setImdbRating(dto.getImdbRating());
        if (dto.getViewCount() != null) entity.setViewCount(dto.getViewCount());
        if (dto.getLikeCount() != null) entity.setLikeCount(dto.getLikeCount());
        if (dto.getDislikeCount() != null) entity.setDislikeCount(dto.getDislikeCount());
        if (dto.getAverageRating() != null) entity.setAverageRating(dto.getAverageRating());
        if (dto.getTotalRatings() != null) entity.setTotalRatings(dto.getTotalRatings());
        if (dto.getCommentCount() != null) entity.setCommentCount(dto.getCommentCount());
        if (dto.getIsFeatured() != null) entity.setIsFeatured(dto.getIsFeatured());
        if (dto.getIsTrending() != null) entity.setIsTrending(dto.getIsTrending());
        if (dto.getReleaseDate() != null) entity.setReleaseDate(dto.getReleaseDate());
        if (dto.getTrailerUrl() != null) entity.setTrailerUrl(dto.getTrailerUrl());
        if (dto.getDownloadEnabled() != null) entity.setDownloadEnabled(dto.getDownloadEnabled());
        if (dto.getMaxDownloadQuality() != null) entity.setMaxDownloadQuality(dto.getMaxDownloadQuality());
        if (dto.getAvailableQualities() != null) entity.setAvailableQualities(new java.util.ArrayList<>(dto.getAvailableQualities()));
    }

    public static MovieDTOs.MovieResponseDTO toResponse(Movie entity) {
        if (entity == null) return null;
        return MovieDTOs.MovieResponseDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .synopsis(entity.getSynopsis())
                .year(entity.getYear())
                .posterUrl(formatImageUrl(entity.getPosterUrl()))
                .thumbnailUrl(formatImageUrl(entity.getThumbnailUrl()))
                .videoUrl(formatVideoUrl(entity.getVideoUrl()))
                .videoFormat(entity.getVideoFormat())
                .videoDuration(entity.getVideoDuration())
                .videoQuality(entity.getVideoQuality())
                .fileSizeBytes(entity.getFileSizeBytes())
                .streamingUrl(entity.getStreamingUrl())
                .isAvailable(entity.getIsAvailable())
                .actors(entity.getActors())
                .directorName(entity.getDirector() != null ? entity.getDirector().getName() : null)
                .country(entity.getCountry() != null ? entity.getCountry().getName() : null)
                .language(entity.getLanguage())
                .ageRating(entity.getAgeRating())
                .imdbRating(entity.getImdbRating())
                .viewCount(entity.getViewCount())
                .likeCount(entity.getLikeCount())
                .dislikeCount(entity.getDislikeCount())
                .averageRating(entity.getAverageRating())
                .totalRatings(entity.getTotalRatings())
                .isFeatured(entity.getIsFeatured())
                .isTrending(entity.getIsTrending())
                .releaseDate(entity.getReleaseDate())
                .trailerUrl(entity.getTrailerUrl())
                .downloadEnabled(entity.getDownloadEnabled())
                .maxDownloadQuality(entity.getMaxDownloadQuality())
                .availableQualities(getAvailableQualities(entity.getId()))
                .categories(entity.getCategories() != null ? entity.getCategories().stream().map(Category::getName).toList() : new java.util.ArrayList<>())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private static Country findCountryByName(String countryName) {
        if (countryName == null || countryName.trim().isEmpty()) {
            return null;
        }
        if (countryRepository == null) {
            return null;
        }
        return countryRepository.findByName(countryName).orElse(null);
    }
    
    private static Director findDirectorByName(String directorName) {
        if (directorName == null || directorName.trim().isEmpty()) {
            return null;
        }
        if (directorRepository == null) {
            return null;
        }
        return directorRepository.findByNameIgnoreCase(directorName.trim()).orElse(null);
    }

    private static java.util.List<String> getAvailableQualities(Long movieId) {
        if (movieId == null || videoResolutionService == null) {
            return new java.util.ArrayList<>();
        }
        try {
            return videoResolutionService.getAvailableQualitiesByMovieId(movieId);
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }

    private static String formatImageUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        if (url.startsWith("/api/images/") || url.startsWith("http")) {
            return url;
        }
        return "/api/images/" + url;
    }

    private static String formatVideoUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        if (url.startsWith("/api/videos/stream/") || url.startsWith("http")) {
            return url;
        }
        return "/api/videos/stream/" + url;
    }
}



