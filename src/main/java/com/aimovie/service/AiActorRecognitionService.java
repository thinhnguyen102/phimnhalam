package com.aimovie.service;

import com.aimovie.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AiActorRecognitionService {

    AiActorRecognitionResponse recognizeActorFromImage(MultipartFile imageFile);
    AiActorRecognitionResponse recognizeActorFromBase64(String imageBase64);
    AiActorRecognitionResponse recognizeActorFromUrl(String imageUrl);
    
    ActorMoviesResponse getActorMovies(String actorName);
    List<MovieSearchDTO> getActorMoviesList(String actorName, int limit);
    
    ActorRecognitionResult recognizeActorAndGetMovies(MultipartFile imageFile);
    ActorRecognitionResult recognizeActorAndGetMoviesFromBase64(String imageBase64);
    ActorRecognitionResult recognizeActorAndGetMoviesFromUrl(String imageUrl);
    
    AiServiceHealth checkAiServiceHealth();
    AiServiceConfig getAiServiceConfig();
    void updateAiServiceConfig(AiServiceConfig config);
    
    List<ActorRecognitionResult> recognizeMultipleActors(List<MultipartFile> imageFiles);
    List<ActorRecognitionResult> recognizeMultipleActorsFromBase64(List<String> imageBase64List);
    
    ActorMoviesResponse getActorStatistics(String actorName);
    List<String> getPopularActors(int limit);
    List<String> getActorsByGenre(String genre, int limit);
}
