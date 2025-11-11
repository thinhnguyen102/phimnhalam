package com.aimovie.controller;

import com.aimovie.dto.AuthDTOs;
import com.aimovie.dto.UserDTOs;
import com.aimovie.service.AuthService;
import com.aimovie.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthDTOs.AuthResponseDTO> login(@Valid @RequestBody AuthDTOs.LoginRequestDTO loginRequest) {
        AuthDTOs.AuthResponseDTO response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTOs.UserResponseDTO> register(@Valid @RequestBody UserDTOs.UserCreateDTO createDTO) {
        UserDTOs.UserResponseDTO response = userService.register(createDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthDTOs.CurrentUserDTO> getCurrentUser(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String token = authService.extractTokenFromHeader(authHeader);
            if (token == null) {
                log.warn("Invalid token format");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            AuthDTOs.CurrentUserDTO currentUser = authService.getCurrentUserFromToken(token);
            return ResponseEntity.ok(currentUser);

        } catch (RuntimeException e) {
            log.error("Error getting current user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Unexpected error getting current user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<ValidationResponse> validateToken(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.ok(new ValidationResponse(false, "Missing or invalid Authorization header", null));
            }

            String token = authService.extractTokenFromHeader(authHeader);
            if (token == null) {
                return ResponseEntity.ok(new ValidationResponse(false, "Invalid token format", null));
            }

            boolean isValid = authService.validateToken(token);
            if (!isValid) {
                return ResponseEntity.ok(new ValidationResponse(false, "Invalid or expired token", null));
            }

            String username = authService.getUsernameFromToken(token);
            return ResponseEntity.ok(new ValidationResponse(true, "Token is valid", username));

        } catch (Exception e) {
            log.error("Error validating token", e);
            return ResponseEntity.ok(new ValidationResponse(false, "Token validation failed", null));
        }
    }

    // Inner class for token validation response
    public static class ValidationResponse {
        private boolean valid;
        private String message;
        private String username;

        public ValidationResponse(boolean valid, String message, String username) {
            this.valid = valid;
            this.message = message;
            this.username = username;
        }

        // Getters
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public String getUsername() { return username; }
    }
}
