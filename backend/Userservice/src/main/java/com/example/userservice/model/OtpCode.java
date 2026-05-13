package com.example.userservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "otp_codes",
        indexes = {
                @Index(name = "idx_otp_codes_email",columnList = "email"),
                @Index(name = "idx_otp_codes_created_at",columnList = "created_at"),
                @Index(name = "idx_otp_codes_status",columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OtpCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false)
    @Email(message = "Invalid email format")
    private String email;

    @Column(nullable = false)
    private String otp;

    @Column(name = "created_at",nullable = false,updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OtpStatus status;


    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

}