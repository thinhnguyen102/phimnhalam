package com.aimovie.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "users",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_users_username", columnNames = {"username"}),
           @UniqueConstraint(name = "uk_users_email", columnNames = {"email"})
       }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper=false)
public class User extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(length = 50, nullable = false)
    private String username;

    @NotBlank
    @Email
    @Size(max = 255)
    @Column(length = 255, nullable = false)
    private String email;

    @Size(max = 255)
    @Column(length = 255)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false, length = 20)
    private Set<Role> roles;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Size(max = 100)
    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Size(max = 500)
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Size(max = 20)
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", length = 20)
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Size(max = 255)
    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Column(name = "email_verified")
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "phone_verified")
    @Builder.Default
    private Boolean phoneVerified = false;

    @Column(name = "last_login_at")
    private java.time.LocalDateTime lastLoginAt;

    @Column(name = "preferred_language", length = 10)
    @Builder.Default
    private String preferredLanguage = "vi";

    @Column(name = "preferred_quality", length = 10)
    @Builder.Default
    private String preferredQuality = "1080p";

    @Column(name = "auto_play")
    @Builder.Default
    private Boolean autoPlay = true;

    @Column(name = "subtitle_enabled")
    @Builder.Default
    private Boolean subtitleEnabled = true;

    public enum AuthProvider {
        LOCAL, GOOGLE, FACEBOOK, APPLE
    }
}

