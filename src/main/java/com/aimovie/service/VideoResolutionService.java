package com.aimovie.service;

import com.aimovie.dto.VideoResolutionDTOs.*;

import java.util.List;

public interface VideoResolutionService {

    VideoResolutionResponse createVideoResolution(VideoResolutionRequest request);

    VideoResolutionResponse updateVideoResolution(Long id, VideoResolutionUpdateRequest request);

    VideoResolutionResponse getVideoResolutionById(Long id);

    List<VideoResolutionResponse> getVideoResolutionsByMovieId(Long movieId);

    List<AvailableResolutionResponse> getAvailableResolutionsByMovieId(Long movieId);

    List<String> getAvailableQualitiesByMovieId(Long movieId);

    VideoResolutionResponse getVideoResolutionByMovieIdAndQuality(Long movieId, String quality);

    ResolutionChangeResponse changeVideoResolution(ResolutionChangeRequest request);

    void deleteVideoResolution(Long id);

    void deleteVideoResolutionsByMovieId(Long movieId);

    List<VideoResolutionResponse> getVideoResolutionsByEncodingStatus(String status);

    VideoResolutionResponse updateEncodingStatus(Long id, String status, Integer progress);

    boolean isResolutionAvailable(Long movieId, String quality);

    String getBestAvailableQuality(Long movieId, String preferredQuality);

    List<AvailableResolutionResponse> getResolutionsByMinHeight(Long movieId, Integer minHeight);
}
