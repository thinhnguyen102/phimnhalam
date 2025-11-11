package com.aimovie.serviceImpl;

import com.aimovie.dto.VideoResolutionDTOs.*;
import com.aimovie.entity.Movie;
import com.aimovie.entity.VideoResolution;
import com.aimovie.repository.MovieRepository;
import com.aimovie.repository.VideoResolutionRepository;
import com.aimovie.service.VideoResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VideoResolutionServiceImpl implements VideoResolutionService {

    private final VideoResolutionRepository videoResolutionRepository;
    private final MovieRepository movieRepository;

    @Override
    public VideoResolutionResponse createVideoResolution(VideoResolutionRequest request) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + request.getMovieId()));

        if (videoResolutionRepository.existsByMovieAndQuality(movie, request.getQuality())) {
            throw new RuntimeException("Video resolution with quality " + request.getQuality() + " already exists for this movie");
        }

        VideoResolution videoResolution = VideoResolution.builder()
                .movie(movie)
                .quality(request.getQuality())
                .width(request.getWidth())
                .height(request.getHeight())
                .videoUrl(request.getVideoUrl())
                .videoFormat(request.getVideoFormat())
                .fileSizeBytes(request.getFileSizeBytes())
                .bitrate(request.getBitrate())
                .isAvailable(request.getIsAvailable())
                .encodingStatus(request.getEncodingStatus())
                .encodingProgress(request.getEncodingProgress())
                .build();

        VideoResolution savedResolution = videoResolutionRepository.save(videoResolution);
        log.info("Created video resolution: {} for movie: {}", request.getQuality(), movie.getTitle());

        return mapToResponse(savedResolution);
    }

    @Override
    public VideoResolutionResponse updateVideoResolution(Long id, VideoResolutionUpdateRequest request) {
        VideoResolution videoResolution = videoResolutionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video resolution not found with id: " + id));

        if (request.getVideoUrl() != null) {
            videoResolution.setVideoUrl(request.getVideoUrl());
        }
        if (request.getVideoFormat() != null) {
            videoResolution.setVideoFormat(request.getVideoFormat());
        }
        if (request.getFileSizeBytes() != null) {
            videoResolution.setFileSizeBytes(request.getFileSizeBytes());
        }
        if (request.getBitrate() != null) {
            videoResolution.setBitrate(request.getBitrate());
        }
        if (request.getIsAvailable() != null) {
            videoResolution.setIsAvailable(request.getIsAvailable());
        }
        if (request.getEncodingStatus() != null) {
            videoResolution.setEncodingStatus(request.getEncodingStatus());
        }
        if (request.getEncodingProgress() != null) {
            videoResolution.setEncodingProgress(request.getEncodingProgress());
        }

        VideoResolution updatedResolution = videoResolutionRepository.save(videoResolution);
        log.info("Updated video resolution: {} for movie: {}", updatedResolution.getQuality(), updatedResolution.getMovie().getTitle());

        return mapToResponse(updatedResolution);
    }

    @Override
    @Transactional(readOnly = true)
    public VideoResolutionResponse getVideoResolutionById(Long id) {
        VideoResolution videoResolution = videoResolutionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video resolution not found with id: " + id));

        return mapToResponse(videoResolution);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoResolutionResponse> getVideoResolutionsByMovieId(Long movieId) {
        List<VideoResolution> resolutions = videoResolutionRepository.findByMovieId(movieId);
        return resolutions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailableResolutionResponse> getAvailableResolutionsByMovieId(Long movieId) {
        List<VideoResolution> resolutions = videoResolutionRepository.findAvailableResolutionsByMovieIdOrderByHeightDesc(movieId);
        return resolutions.stream()
                .map(this::mapToAvailableResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAvailableQualitiesByMovieId(Long movieId) {
        return videoResolutionRepository.findAvailableQualitiesByMovieId(movieId);
    }

    @Override
    @Transactional(readOnly = true)
    public VideoResolutionResponse getVideoResolutionByMovieIdAndQuality(Long movieId, String quality) {
        VideoResolution videoResolution = videoResolutionRepository.findByMovieIdAndQualityAndIsAvailableTrue(movieId, quality)
                .orElseThrow(() -> new RuntimeException("Video resolution not found for movie id: " + movieId + " and quality: " + quality));

        return mapToResponse(videoResolution);
    }

    @Override
    public ResolutionChangeResponse changeVideoResolution(ResolutionChangeRequest request) {
        VideoResolution videoResolution = videoResolutionRepository.findByMovieIdAndQualityAndIsAvailableTrue(request.getMovieId(), request.getQuality())
                .orElseThrow(() -> new RuntimeException("Video resolution not found for movie id: " + request.getMovieId() + " and quality: " + request.getQuality()));

        if (!videoResolution.getIsAvailable()) {
            return ResolutionChangeResponse.builder()
                    .success(false)
                    .message("Requested quality is not available")
                    .build();
        }

        log.info("Changed video resolution to {} for movie id: {}", request.getQuality(), request.getMovieId());

        return ResolutionChangeResponse.builder()
                .newStreamingUrl(videoResolution.getVideoUrl())
                .quality(videoResolution.getQuality())
                .width(videoResolution.getWidth())
                .height(videoResolution.getHeight())
                .bitrate(videoResolution.getBitrate())
                .success(true)
                .message("Resolution changed successfully")
                .build();
    }

    @Override
    public void deleteVideoResolution(Long id) {
        VideoResolution videoResolution = videoResolutionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video resolution not found with id: " + id));

        videoResolutionRepository.delete(videoResolution);
        log.info("Deleted video resolution: {} for movie: {}", videoResolution.getQuality(), videoResolution.getMovie().getTitle());
    }

    @Override
    public void deleteVideoResolutionsByMovieId(Long movieId) {
        List<VideoResolution> resolutions = videoResolutionRepository.findByMovieId(movieId);
        videoResolutionRepository.deleteAll(resolutions);
        log.info("Deleted {} video resolutions for movie id: {}", resolutions.size(), movieId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoResolutionResponse> getVideoResolutionsByEncodingStatus(String status) {
        List<VideoResolution> resolutions = videoResolutionRepository.findByEncodingStatus(status);
        return resolutions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public VideoResolutionResponse updateEncodingStatus(Long id, String status, Integer progress) {
        VideoResolution videoResolution = videoResolutionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video resolution not found with id: " + id));

        videoResolution.setEncodingStatus(status);
        if (progress != null) {
            videoResolution.setEncodingProgress(progress);
        }

        VideoResolution updatedResolution = videoResolutionRepository.save(videoResolution);
        log.info("Updated encoding status to {} for video resolution: {}", status, updatedResolution.getQuality());

        return mapToResponse(updatedResolution);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isResolutionAvailable(Long movieId, String quality) {
        return videoResolutionRepository.findByMovieIdAndQualityAndIsAvailableTrue(movieId, quality).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public String getBestAvailableQuality(Long movieId, String preferredQuality) {
        List<String> availableQualities = getAvailableQualitiesByMovieId(movieId);
        
        if (availableQualities.isEmpty()) {
            return "720p";
        }

        if (availableQualities.contains(preferredQuality)) {
            return preferredQuality;
        }

        List<String> standardQualities = List.of("1080p", "720p", "360p");
        for (String quality : standardQualities) {
            if (availableQualities.contains(quality)) {
                return quality;
            }
        }

        return availableQualities.get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailableResolutionResponse> getResolutionsByMinHeight(Long movieId, Integer minHeight) {
        List<VideoResolution> resolutions = videoResolutionRepository.findAvailableResolutionsByMovieIdAndMinHeight(movieId, minHeight);
        return resolutions.stream()
                .map(this::mapToAvailableResponse)
                .collect(Collectors.toList());
    }

    private VideoResolutionResponse mapToResponse(VideoResolution videoResolution) {
        return VideoResolutionResponse.builder()
                .id(videoResolution.getId())
                .movieId(videoResolution.getMovie().getId())
                .quality(videoResolution.getQuality())
                .width(videoResolution.getWidth())
                .height(videoResolution.getHeight())
                .videoUrl(videoResolution.getVideoUrl())
                .videoFormat(videoResolution.getVideoFormat())
                .fileSizeBytes(videoResolution.getFileSizeBytes())
                .bitrate(videoResolution.getBitrate())
                .isAvailable(videoResolution.getIsAvailable())
                .encodingStatus(videoResolution.getEncodingStatus())
                .encodingProgress(videoResolution.getEncodingProgress())
                .movieTitle(videoResolution.getMovie().getTitle())
                .build();
    }

    private AvailableResolutionResponse mapToAvailableResponse(VideoResolution videoResolution) {
        return AvailableResolutionResponse.builder()
                .quality(videoResolution.getQuality())
                .width(videoResolution.getWidth())
                .height(videoResolution.getHeight())
                .videoUrl(videoResolution.getVideoUrl())
                .videoFormat(videoResolution.getVideoFormat())
                .fileSizeBytes(videoResolution.getFileSizeBytes())
                .bitrate(videoResolution.getBitrate())
                .isAvailable(videoResolution.getIsAvailable())
                .encodingStatus(videoResolution.getEncodingStatus())
                .build();
    }
}
