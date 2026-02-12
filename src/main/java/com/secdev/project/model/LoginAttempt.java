package com.secdev.project.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for tracking login attempts to implement anti-brute-force protection
 */
@Entity
@Table(name = "login_attempts")
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private LocalDateTime attemptTime;

    @Column(nullable = false)
    private boolean successful;

    @Column(length = 45)
    private String ipAddress;

    // Constructors
    public LoginAttempt() {
    }

    public LoginAttempt(String email, boolean successful, String ipAddress) {
        this.email = email;
        this.successful = successful;
        this.ipAddress = ipAddress;
        this.attemptTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getAttemptTime() {
        return attemptTime;
    }

    public void setAttemptTime(LocalDateTime attemptTime) {
        this.attemptTime = attemptTime;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public String toString() {
        return "LoginAttempt{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", attemptTime=" + attemptTime +
                ", successful=" + successful +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }
}
