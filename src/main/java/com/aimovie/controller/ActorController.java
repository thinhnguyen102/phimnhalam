package com.aimovie.controller;

import com.aimovie.dto.ActorCRUD;
import com.aimovie.dto.MovieDTOs;
import com.aimovie.service.ActorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class ActorController {

    private final ActorService actorService;

    // Public endpoints
    @GetMapping("/api/actors")
    public ResponseEntity<Page<ActorCRUD.Response>> list(
            @RequestParam(value = "q", required = false) String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(actorService.list(q, pageable));
    }

    @GetMapping("/api/actors/{id}")
    public ResponseEntity<ActorCRUD.Response> get(@PathVariable Long id) {
        ActorCRUD.Response res = actorService.get(id);
        return res != null ? ResponseEntity.ok(res) : ResponseEntity.notFound().build();
    }

    @GetMapping("/api/actors/{id}/movies")
    public ResponseEntity<Page<MovieDTOs.MovieResponseDTO>> listMovies(
            @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        try {
            return ResponseEntity.ok(actorService.listMovies(id, pageable));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Admin endpoints
    @PostMapping("/api/admin/actors")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR') or hasRole('UPLOADER')")
    public ResponseEntity<ActorCRUD.Response> create(
            @RequestParam("name") String name,
            @RequestParam(value = "biography", required = false) String biography,
            @RequestParam(value = "birthDate", required = false) String birthDate,
            @RequestParam(value = "nationality", required = false) String nationality,
            @RequestParam(value = "profileImageUrl", required = false) String profileImageUrl,
            @RequestParam(value = "isActive", required = false) Boolean isActive) {
        try {
            ActorCRUD.CreateRequest req = new ActorCRUD.CreateRequest(
                    name,
                    profileImageUrl,
                    birthDate != null && !birthDate.isEmpty() ? java.time.LocalDate.parse(birthDate) : null,
                    biography
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(actorService.create(req));
        } catch (RuntimeException e) {
            log.warn("Create actor failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/api/admin/actors/form")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR') or hasRole('UPLOADER')")
    public ResponseEntity<ActorCRUD.Response> createForm(
            @RequestParam("name") String name,
            @RequestParam(value = "dob", required = false) java.time.LocalDate dob,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            ActorCRUD.CreateRequest req = new ActorCRUD.CreateRequest(name, null, dob, description);
            ActorCRUD.Response created = actorService.create(req);
            if (file != null && !file.isEmpty()) {
                created = actorService.uploadImage(created.getId(), file);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            log.warn("Create actor (form) failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Error creating actor via form", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/api/admin/actors/{id}/image")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR') or hasRole('UPLOADER')")
    public ResponseEntity<ActorCRUD.Response> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(actorService.uploadImage(id, file));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/api/admin/actors/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR') or hasRole('UPLOADER')")
    public ResponseEntity<ActorCRUD.Response> update(
            @PathVariable Long id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "biography", required = false) String biography,
            @RequestParam(value = "birthDate", required = false) String birthDate,
            @RequestParam(value = "nationality", required = false) String nationality,
            @RequestParam(value = "profileImageUrl", required = false) String profileImageUrl,
            @RequestParam(value = "isActive", required = false) Boolean isActive,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            ActorCRUD.UpdateRequest req = new ActorCRUD.UpdateRequest(
                    name,
                    profileImageUrl,
                    birthDate != null && !birthDate.isEmpty() ? java.time.LocalDate.parse(birthDate) : null,
                    biography
            );
            
            ActorCRUD.Response updated = actorService.update(id, req);
            if (file != null && !file.isEmpty()) {
                updated = actorService.uploadImage(id, file);
            }
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/api/admin/actors/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            actorService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}


