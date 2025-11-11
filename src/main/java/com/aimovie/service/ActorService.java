package com.aimovie.service;

import com.aimovie.dto.ActorCRUD;
import com.aimovie.dto.MovieDTOs;
import com.aimovie.entity.Actor;
import com.aimovie.entity.Movie;
import com.aimovie.mapper.MovieMapper;
import com.aimovie.repository.ActorRepository;
import com.aimovie.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class ActorService {

    private final ActorRepository actorRepository;
    private final MovieRepository movieRepository;
    private final FileUploadService fileUploadService;

    @Transactional(readOnly = true)
    public Page<ActorCRUD.Response> list(String q, Pageable pageable) {
        Page<Actor> page = (q == null || q.isBlank())
                ? actorRepository.findAll(pageable)
                : actorRepository.findByNameContainingIgnoreCase(q, pageable);
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ActorCRUD.Response get(Long id) {
        return actorRepository.findById(id).map(this::toResponse).orElse(null);
    }

    public ActorCRUD.Response create(ActorCRUD.CreateRequest req) {
        String normalized = req.getName().trim();
        if (actorRepository.existsByName(normalized)) {
            throw new RuntimeException("Actor name already exists");
        }
        Actor actor = Actor.builder()
                .name(normalized)
                .imageUrl(req.getImageUrl())
                .dob(req.getDob())
                .description(req.getDescription())
                .build();
        return toResponse(actorRepository.save(actor));
    }

    public ActorCRUD.Response update(Long id, ActorCRUD.UpdateRequest req) {
        Actor actor = actorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Actor not found"));
        if (req.getName() != null) actor.setName(req.getName());
        if (req.getImageUrl() != null) actor.setImageUrl(req.getImageUrl());
        if (req.getDob() != null) actor.setDob(req.getDob());
        if (req.getDescription() != null) actor.setDescription(req.getDescription());
        return toResponse(actorRepository.save(actor));
    }

    public void delete(Long id) {
        Actor actor = actorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Actor not found"));
        actorRepository.delete(actor);
    }

    public ActorCRUD.Response uploadImage(Long id, MultipartFile file) {
        Actor actor = actorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Actor not found"));
        try {
            String filename = fileUploadService.uploadImageFile(file);
            String imageUrl = fileUploadService.buildPublicImageUrl(filename);
            actor.setImageUrl(imageUrl);
            return toResponse(actorRepository.save(actor));
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload actor image", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<MovieDTOs.MovieResponseDTO> listMovies(Long actorId, Pageable pageable) {
        Actor actor = actorRepository.findById(actorId)
                .orElseThrow(() -> new RuntimeException("Actor not found"));
        Page<Movie> page = movieRepository.findByActorsContaining(actor.getName(), pageable);
        return page.map(MovieMapper::toResponse);
    }

    private ActorCRUD.Response toResponse(Actor a) {
        int movieCount = 0;
        try {
            // Prefer counting via Movie.actors list of names to reflect current data model
            movieCount = Math.toIntExact(movieRepository
                    .findByActorsContaining(a.getName(), org.springframework.data.domain.PageRequest.of(0, 1))
                    .getTotalElements());
        } catch (Exception ignored) {
            movieCount = a.getMovies() != null ? a.getMovies().size() : 0;
        }
        return ActorCRUD.Response.builder()
                .id(a.getId())
                .name(a.getName())
                .imageUrl(a.getImageUrl())
                .dob(a.getDob())
                .description(a.getDescription())
                .movieCount(movieCount)
                .build();
    }
}


