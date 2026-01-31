package tn.tn.elfatoora.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sms_otp", indexes = {
        @Index(name="idx_otp_email", columnList = "email"),
        @Index(name="idx_otp_phone_number", columnList = "phone_number"),
        @Index(name="idx_otp_expires", columnList = "expires_at")
})
public class SmsOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=255)
    private String email;

    @Column(name="phone_number",nullable=false, length=50, unique = true)
    private String phoneNumber;


    @Column(nullable=false, length=20)
    private String code;

    @Column(name="expires_at", nullable=false)
    private LocalDateTime expiresAt;

    @Column(nullable=false)
    private int attempts = 0;

    @Column(nullable=false)
    private boolean consumed = false;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters/Setters
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public boolean isConsumed() { return consumed; }
    public void setConsumed(boolean consumed) { this.consumed = consumed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
