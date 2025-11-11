package com.aimovie.controller;

import com.aimovie.dto.ApiResponse;
import com.aimovie.dto.DirectorDTO;
import com.aimovie.service.DirectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/directors")
@RequiredArgsConstructor
@Slf4j
public class DirectorController {
    
    private final DirectorService directorService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<DirectorDTO>> createDirector(
            @RequestParam("name") String name,
            @RequestParam(value = "biography", required = false) String biography,
            @RequestParam(value = "birthDate", required = false) String birthDate,
            @RequestParam(value = "nationality", required = false) String nationality,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {
        
        try {
            log.info("Creating director: {}", name);
            
            DirectorDTO director = directorService.createDirector(name, biography, birthDate, nationality, photo);
            
            ApiResponse<DirectorDTO> response = new ApiResponse<>("SUCCESS", "Director created successfully", director);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("Error creating director", e);
            ApiResponse<DirectorDTO> response = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @PutMapping("/{directorId}")
    public ResponseEntity<ApiResponse<DirectorDTO>> updateDirector(
            @PathVariable Long directorId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "biography", required = false) String biography,
            @RequestParam(value = "birthDate", required = false) String birthDate,
            @RequestParam(value = "nationality", required = false) String nationality,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {
        
        try {
            log.info("Updating director with ID: {}", directorId);
            
            DirectorDTO director = directorService.updateDirector(directorId, name, biography, birthDate, nationality, photo);
            
            ApiResponse<DirectorDTO> response = new ApiResponse<>("SUCCESS", "Director updated successfully", director);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error updating director with ID: {}", directorId, e);
            ApiResponse<DirectorDTO> response = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @GetMapping("/{directorId}")
    public ResponseEntity<ApiResponse<DirectorDTO>> getDirectorById(@PathVariable Long directorId) {
        try {
            log.info("Getting director with ID: {}", directorId);
            
            DirectorDTO director = directorService.getDirectorById(directorId);
            
            ApiResponse<DirectorDTO> response = new ApiResponse<>("SUCCESS", "Director retrieved successfully", director);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting director with ID: {}", directorId, e);
            ApiResponse<DirectorDTO> response = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getAllDirectors(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "name") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction) {
        
        try {
            log.info("Getting all directors - page: {}, size: {}, sort: {}, direction: {}", page, size, sort, direction);
            
            Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            
            Page<DirectorDTO> directors = directorService.getAllDirectors(pageable);
            
            ApiResponse<Object> response = new ApiResponse<>("SUCCESS", "Directors retrieved successfully", directors);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting all directors", e);
            ApiResponse<Object> response = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<DirectorDTO>>> getAllDirectorsList() {
        try {
            log.info("Getting all directors list");
            
            List<DirectorDTO> directors = directorService.getAllDirectors();
            
            ApiResponse<List<DirectorDTO>> response = new ApiResponse<>("SUCCESS", "Directors retrieved successfully", directors);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting all directors list", e);
            ApiResponse<List<DirectorDTO>> response = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @DeleteMapping("/{directorId}")
    public ResponseEntity<ApiResponse<Void>> deleteDirector(@PathVariable Long directorId) {
        try {
            log.info("Deleting director with ID: {}", directorId);
            
            directorService.deleteDirector(directorId);
            
            ApiResponse<Void> response = new ApiResponse<>("SUCCESS", "Director deleted successfully", null);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error deleting director with ID: {}", directorId, e);
            ApiResponse<Void> response = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Object>> searchDirectors(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "nationality", required = false) String nationality,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "name") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction) {
        
        try {
            log.info("Searching directors - name: {}, nationality: {}, page: {}, size: {}", name, nationality, page, size);
            
            Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            
            Page<DirectorDTO> directors = directorService.searchDirectors(name, nationality, pageable);
            
            ApiResponse<Object> response = new ApiResponse<>("SUCCESS", "Directors search completed", directors);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error searching directors", e);
            ApiResponse<Object> response = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/by-nationality/{nationality}")
    public ResponseEntity<ApiResponse<List<DirectorDTO>>> getDirectorsByNationality(@PathVariable String nationality) {
        try {
            log.info("Getting directors by nationality: {}", nationality);
            
            List<DirectorDTO> directors = directorService.getDirectorsByNationality(nationality);
            
            ApiResponse<List<DirectorDTO>> response = new ApiResponse<>("SUCCESS", "Directors retrieved successfully", directors);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting directors by nationality: {}", nationality, e);
            ApiResponse<List<DirectorDTO>> response = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<DirectorDTO>>> getActiveDirectors() {
        try {
            log.info("Getting active directors");
            
            List<DirectorDTO> directors = directorService.getActiveDirectors();
            
            ApiResponse<List<DirectorDTO>> response = new ApiResponse<>("SUCCESS", "Active directors retrieved successfully", directors);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting active directors", e);
            ApiResponse<List<DirectorDTO>> response = new ApiResponse<>("ERROR", e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
