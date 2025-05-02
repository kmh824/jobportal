// src/main/java/com/jobboard/jobportal/entity/EmailVerification.java
package com.jobboard.jobportal.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import lombok.*;

@Entity
@Table(
        name = "email_verification",
        indexes = {
                @Index(name = "idx_ev_email", columnList = "email"),
                @Index(name = "idx_ev_expires_at", columnList = "expires_at")
        },
        uniqueConstraints = @UniqueConstraint(name = "uc_ev_email", columnNames = "email")
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;

    @Column(name = "blocked_until")
    private LocalDateTime blockedUntil;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
