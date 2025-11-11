package com.aimovie.controller;

import com.aimovie.dto.UserDTOs;
import com.aimovie.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDTOs.UserResponseDTO> createUser(@Valid @RequestBody UserDTOs.UserCreateDTO createDTO) {
        UserDTOs.UserResponseDTO response = userService.register(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<UserDTOs.UserResponseDTO>> getAllUsers() {
        List<UserDTOs.UserResponseDTO> response = userService.getAllUsers();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTOs.UserResponseDTO> getUserById(@PathVariable Long id) {
        UserDTOs.UserResponseDTO response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTOs.UserResponseDTO> getUserByUsername(@PathVariable String username) {
        UserDTOs.UserResponseDTO response = userService.getUserByUsername(username);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTOs.UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTOs.UserUpdateDTO updateDTO) {
        UserDTOs.UserResponseDTO response = userService.updateUser(id, updateDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/change-password")
    public ResponseEntity<UserDTOs.UserResponseDTO> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody UserDTOs.ChangePasswordDTO changePasswordDTO) {
        UserDTOs.UserResponseDTO response = userService.changePassword(id, changePasswordDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/avatar")
    public ResponseEntity<UserDTOs.UserResponseDTO> uploadAvatar(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            UserDTOs.UserResponseDTO response = userService.uploadAvatar(id, file);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Failed to upload avatar for user {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
