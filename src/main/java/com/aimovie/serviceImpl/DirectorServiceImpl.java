package com.aimovie.serviceImpl;

import com.aimovie.dto.DirectorDTO;
import com.aimovie.entity.Director;
import com.aimovie.repository.DirectorRepository;
import com.aimovie.service.DirectorService;
import com.aimovie.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DirectorServiceImpl implements DirectorService {
    
    private final DirectorRepository directorRepository;
    private final FileUploadService fileUploadService;
    
    @Override
    public DirectorDTO createDirector(String name, String biography, String birthDate, 
                                     String nationality, MultipartFile photo) {
        log.info("Creating director with name: {}", name);
        
        if (directorRepository.existsByNameIgnoreCase(name)) {
            throw new RuntimeException("Director with name '" + name + "' already exists");
        }
        
        LocalDate parsedBirthDate = null;
        if (birthDate != null && !birthDate.trim().isEmpty()) {
            try {
                parsedBirthDate = LocalDate.parse(birthDate, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                throw new RuntimeException("Invalid birth date format. Use YYYY-MM-DD");
            }
        }
        
        String photoUrl = null;
        if (photo != null && !photo.isEmpty()) {
            try {
                String photoFilename = fileUploadService.uploadImageFile(photo);
                photoUrl = fileUploadService.buildPublicImageUrl(photoFilename);
                log.info("Uploaded director photo: {}", photoUrl);
            } catch (Exception e) {
                log.error("Failed to upload director photo", e);
                throw new RuntimeException("Failed to upload director photo: " + e.getMessage());
            }
        }
        
        // Create director entity
        Director director = Director.builder()
                .name(name.trim())
                .biography(biography != null ? biography.trim() : null)
                .birthDate(parsedBirthDate)
                .nationality(nationality != null ? nationality.trim() : null)
                .photoUrl(photoUrl)
                .isActive(true)
                .build();
        
        Director savedDirector = directorRepository.save(director);
        log.info("Created director with ID: {}", savedDirector.getId());
        
        return toDTO(savedDirector);
    }
    
    @Override
    public DirectorDTO updateDirector(Long directorId, String name, String biography, 
                                     String birthDate, String nationality, MultipartFile photo) {
        log.info("Updating director with ID: {}", directorId);
        
        Director director = directorRepository.findById(directorId)
                .orElseThrow(() -> new RuntimeException("Director not found with ID: " + directorId));
        
        // Validate name uniqueness (excluding current director)
        if (name != null && !name.trim().isEmpty()) {
            String trimmedName = name.trim();
            if (!director.getName().equalsIgnoreCase(trimmedName) && 
                directorRepository.existsByNameIgnoreCase(trimmedName)) {
                throw new RuntimeException("Director with name '" + trimmedName + "' already exists");
            }
            director.setName(trimmedName);
        }
        
        // Update biography
        if (biography != null) {
            director.setBiography(biography.trim().isEmpty() ? null : biography.trim());
        }
        
        // Update birth date
        if (birthDate != null) {
            if (birthDate.trim().isEmpty()) {
                director.setBirthDate(null);
            } else {
                try {
                    LocalDate parsedBirthDate = LocalDate.parse(birthDate, DateTimeFormatter.ISO_LOCAL_DATE);
                    director.setBirthDate(parsedBirthDate);
                } catch (DateTimeParseException e) {
                    throw new RuntimeException("Invalid birth date format. Use YYYY-MM-DD");
                }
            }
        }
        
        // Update nationality
        if (nationality != null) {
            director.setNationality(nationality.trim().isEmpty() ? null : nationality.trim());
        }
        
        // Update photo if provided
        if (photo != null && !photo.isEmpty()) {
            try {
                String photoFilename = fileUploadService.uploadImageFile(photo);
                director.setPhotoUrl(fileUploadService.buildPublicImageUrl(photoFilename));
                log.info("Updated director photo: {}", director.getPhotoUrl());
            } catch (Exception e) {
                log.error("Failed to upload director photo", e);
                throw new RuntimeException("Failed to upload director photo: " + e.getMessage());
            }
        }
        
        Director savedDirector = directorRepository.save(director);
        log.info("Updated director with ID: {}", savedDirector.getId());
        
        return toDTO(savedDirector);
    }
    
    @Override
    @Transactional(readOnly = true)
    public DirectorDTO getDirectorById(Long directorId) {
        Director director = directorRepository.findById(directorId)
                .orElseThrow(() -> new RuntimeException("Director not found with ID: " + directorId));
        return toDTO(director);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DirectorDTO> getAllDirectors() {
        List<Director> directors = directorRepository.findByIsActiveTrue();
        return directors.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<DirectorDTO> getAllDirectors(Pageable pageable) {
        Page<Director> directors = directorRepository.findByIsActiveTrue(pageable);
        return directors.map(this::toDTO);
    }
    
    @Override
    public void deleteDirector(Long directorId) {
        log.info("Deleting director with ID: {}", directorId);
        
        Director director = directorRepository.findById(directorId)
                .orElseThrow(() -> new RuntimeException("Director not found with ID: " + directorId));
        
        // Check if director has movies
        Long movieCount = directorRepository.countMoviesByDirectorId(directorId);
        if (movieCount > 0) {
            throw new RuntimeException("Cannot delete director. Director has " + movieCount + " movies associated.");
        }
        
        // Soft delete by setting isActive to false
        director.setIsActive(false);
        directorRepository.save(director);
        
        log.info("Soft deleted director with ID: {}", directorId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DirectorDTO> searchDirectorsByName(String name) {
        List<Director> directors = directorRepository.findByNameContainingIgnoreCase(name);
        return directors.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DirectorDTO> getDirectorsByNationality(String nationality) {
        List<Director> directors = directorRepository.findByNationalityIgnoreCase(nationality);
        return directors.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<DirectorDTO> searchDirectors(String name, String nationality, Pageable pageable) {
        Page<Director> directors = directorRepository.searchDirectors(name, nationality, pageable);
        return directors.map(this::toDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DirectorDTO> getActiveDirectors() {
        List<Director> directors = directorRepository.findByIsActiveTrue();
        return directors.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return directorRepository.existsByNameIgnoreCase(name);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long directorId) {
        return directorRepository.existsById(directorId);
    }
    
    private DirectorDTO toDTO(Director director) {
        Long movieCount = directorRepository.countMoviesByDirectorId(director.getId());
        
        return DirectorDTO.builder()
                .id(director.getId())
                .name(director.getName())
                .biography(director.getBiography())
                .birthDate(director.getBirthDate())
                .nationality(director.getNationality())
                .photoUrl(director.getPhotoUrl())
                .isActive(director.getIsActive())
                .createdAt(director.getCreatedAt())
                .updatedAt(director.getUpdatedAt())
                .movieCount(movieCount)
                .build();
    }
}
