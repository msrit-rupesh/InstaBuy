package com.example.userservice.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name="users",
        indexes = {
                @Index(name = "idx_users_username",columnList = "username"),
                @Index(name = "idx_users_role_id", columnList = "role_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        }
)
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Username is required")
    private String username;

    @Column(nullable = false)
    @NotBlank(message = "Name is required")
    private String name;

    @Column(nullable = false)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "Password is required")
    private String password;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified =false;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts=0;

    @Column(name = "last_login_in")
    private Instant lastLoginIn;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "password_updated_at")
    private Instant passwordUpdatedAt;


    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;


    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "profile_id", nullable = true)
    @JsonManagedReference
    private Profile profile;






}