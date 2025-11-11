package com.aimovie.service;

import com.aimovie.dto.DirectorDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DirectorService {
    
    // CRUD Operations
    DirectorDTO createDirector(String name, String biography, String birthDate, 
                              String nationality, MultipartFile photo);
    
    DirectorDTO updateDirector(Long directorId, String name, String biography, 
                              String birthDate, String nationality, MultipartFile photo);
    
    DirectorDTO getDirectorById(Long directorId);
    
    List<DirectorDTO> getAllDirectors();
    
    Page<DirectorDTO> getAllDirectors(Pageable pageable);
    
    void deleteDirector(Long directorId);
    
    // Search and Filter Operations
    List<DirectorDTO> searchDirectorsByName(String name);
    
    List<DirectorDTO> getDirectorsByNationality(String nationality);
    
    Page<DirectorDTO> searchDirectors(String name, String nationality, Pageable pageable);
    
    List<DirectorDTO> getActiveDirectors();
    
    // Validation
    boolean existsByName(String name);
    
    boolean existsById(Long directorId);
}
