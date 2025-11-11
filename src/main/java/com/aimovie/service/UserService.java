package com.aimovie.service;

import com.aimovie.dto.UserDTOs;
import com.aimovie.entity.Role;
import com.aimovie.entity.User;
import com.aimovie.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileUploadService fileUploadService;

    public UserDTOs.UserResponseDTO register(UserDTOs.UserCreateDTO createDTO) {
        if (userRepository.existsByUsernameOrEmail(createDTO.getUsername(), createDTO.getEmail())) {
            throw new RuntimeException("Username or email already exists");
        }

        User user = User.builder()
                .username(createDTO.getUsername())
                .email(createDTO.getEmail())
                .password(passwordEncoder.encode(createDTO.getPassword()))
                .fullName(createDTO.getFullName())
                .birthday(createDTO.getBirthday())
                .roles(Set.of(Role.USER))
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        return toResponseDTO(savedUser);
    }

    public UserDTOs.UserResponseDTO updateUser(Long userId, UserDTOs.UserUpdateDTO updateDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updateDTO.getEmail() != null) {
            // Check if email is already taken by another user
            if (userRepository.existsByEmailAndIdNot(updateDTO.getEmail(), userId)) {
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getFullName() != null) {
            user.setFullName(updateDTO.getFullName());
        }
        if (updateDTO.getBirthday() != null) {
            user.setBirthday(updateDTO.getBirthday());
        }
        if (updateDTO.getAvatarUrl() != null) {
            user.setAvatarUrl(updateDTO.getAvatarUrl());
        }

        User savedUser = userRepository.save(user);
        return toResponseDTO(savedUser);
    }

    public UserDTOs.UserResponseDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toResponseDTO(user);
    }

    public UserDTOs.UserResponseDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toResponseDTO(user);
    }

    public List<UserDTOs.UserResponseDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Delete avatar file if exists
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            try {
                String filename = extractFilenameFromUrl(user.getAvatarUrl());
                fileUploadService.deleteImageFile(filename);
            } catch (IOException e) {
                log.warn("Failed to delete avatar file for user {}: {}", userId, e.getMessage());
            }
        }
        
        userRepository.delete(user);
    }

    public UserDTOs.UserResponseDTO changePassword(Long userId, UserDTOs.ChangePasswordDTO changePasswordDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate current password
        if (!passwordEncoder.matches(changePasswordDTO.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Validate new password confirmation
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            throw new RuntimeException("New password and confirmation do not match");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        User savedUser = userRepository.save(user);
        
        return toResponseDTO(savedUser);
    }

    public UserDTOs.UserResponseDTO uploadAvatar(Long userId, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Delete old avatar if exists
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            try {
                String oldFilename = extractFilenameFromUrl(user.getAvatarUrl());
                fileUploadService.deleteImageFile(oldFilename);
            } catch (IOException e) {
                log.warn("Failed to delete old avatar file for user {}: {}", userId, e.getMessage());
            }
        }

        // Upload new avatar
        String filename = fileUploadService.uploadImageFile(file);
        String avatarUrl = fileUploadService.buildPublicImageUrl(filename);
        
        user.setAvatarUrl(avatarUrl);
        User savedUser = userRepository.save(user);
        
        return toResponseDTO(savedUser);
    }

    private String extractFilenameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        return url.substring(url.lastIndexOf("/") + 1);
    }

    private UserDTOs.UserResponseDTO toResponseDTO(User user) {
        return UserDTOs.UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .birthday(user.getBirthday())
                .avatarUrl(user.getAvatarUrl())
                .roles(user.getRoles())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
