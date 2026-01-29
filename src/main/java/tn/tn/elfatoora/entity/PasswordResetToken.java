package tn.tn.elfatoora.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_token",
       indexes = {
           @Index(name = "idx_prt_user_id", columnList = "user_id"),
           @Index(name = "idx_prt_expires", columnList = "expires_at")
       })
public class PasswordResetToken { 

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(nullable=false, unique=true, length=64)
    private String token;

    @Column(name="expires_at", nullable=false)
    private LocalDateTime expiresAt;

    @Column(nullable=false)
    private boolean used = false;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isUsable() {
        return !used && !isExpired();
    }

    // getters / setters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
}
