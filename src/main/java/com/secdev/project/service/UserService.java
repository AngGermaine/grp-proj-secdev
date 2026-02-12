package com.secdev.project.service;

import com.secdev.project.config.BruteForceProperties;
import com.secdev.project.dto.RegisterRequest;

import com.secdev.project.model.LoginAttempt;
import com.secdev.project.model.Role;
import com.secdev.project.model.User;

import com.secdev.project.repo.LoginAttemptRepository;
import com.secdev.project.repo.UserRepository;

import com.secdev.project.service.exceptions.BadRequestException;
import com.secdev.project.service.exceptions.TooManyAttemptsException;

import org.apache.tika.Tika;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;


import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {     

    private static final long MAX_PHOTO_BYTES = 5L * 1024 * 1024; 

    private static final Set<String> ALLOWED_IMAGE_MIME = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private final UserRepository userRepository;
    private final BruteForceProperties bruteForceProperties;
    private final LoginAttemptRepository loginAttemptRepository;
    private final PasswordEncoder passwordEncoder;
    private final Tika tika = new Tika();

    @Value("${file.upload-dir}")
    private String uploadDir;

    public UserService(UserRepository userRepository,
                   LoginAttemptRepository loginAttemptRepository,
                   PasswordEncoder passwordEncoder,
                   BruteForceProperties bruteForceProperties) {
    this.userRepository = userRepository;
    this.loginAttemptRepository = loginAttemptRepository;
    this.passwordEncoder = passwordEncoder;
    this.bruteForceProperties = bruteForceProperties;
}


    @Transactional
    public void lockAccount(String email) {
        String normalized = normalizeEmail(email);
        userRepository.findByEmail(normalized).ifPresent(u -> {
            if (u.isAccountNonLocked()) {
                u.setAccountNonLocked(false);
                userRepository.save(u);
            }
        });
    }

    public User register(RegisterRequest req, MultipartFile profilePhoto) throws IOException {
        String email = normalizeEmail(req.getEmail());

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Registration failed. Please try again.");
        }

        User user = new User();
        user.setFullName(req.getFullName().trim());
        user.setEmail(email);
        user.setPhoneNumber(req.getPhoneNumber().trim());
        user.setRole(Role.USER);
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setPassword(passwordEncoder.encode(req.getPassword()));

        if (profilePhoto != null && !profilePhoto.isEmpty()) {
            String storedFileName = storeProfilePhoto(profilePhoto);
            user.setProfilePhotoPath(storedFileName);
        }

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("Registration failed. Please try again.");
        }
    }

    public void assertNotBlocked(String email, String ipAddress) {
        LocalDateTime after = LocalDateTime.now().minusMinutes(bruteForceProperties.getWindowMinutes());
        String normalizedEmail = normalizeEmail(email);

        long emailFails = loginAttemptRepository
                .countByEmailAndSuccessfulIsFalseAndAttemptTimeAfter(normalizedEmail, after);

        long ipFails = loginAttemptRepository
                .countByIpAddressAndSuccessfulIsFalseAndAttemptTimeAfter(ipAddress, after);

        if (emailFails >= bruteForceProperties.getMaxEmailAttempts() || ipFails >= bruteForceProperties.getMaxIpAttempts()) {
            throw new TooManyAttemptsException("Too many login attempts. Try again later.");
        }
    }

    public void recordLoginAttempt(String email, boolean success, String ipAddress) {
        LoginAttempt attempt = new LoginAttempt(normalizeEmail(email), success, ipAddress);
        loginAttemptRepository.save(attempt);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(normalizeEmail(email));
    }

    private String storeProfilePhoto(MultipartFile file) throws IOException {
        if (file.getSize() > MAX_PHOTO_BYTES) {
            throw new BadRequestException("Profile photo is too large (max 5MB).");
        }

        String mime = tika.detect(file.getInputStream());
        if (!ALLOWED_IMAGE_MIME.contains(mime)) {
            throw new BadRequestException("Invalid profile photo type. Allowed: JPG, PNG, WEBP.");
        }

        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);

        String ext = mimeToExtension(mime);
        String storedName = "pp_" + System.currentTimeMillis() + "_" + randomSuffix() + ext;

        Path dest = dir.resolve(storedName).normalize();

        if (!dest.startsWith(dir)) {
            throw new BadRequestException("Invalid file path.");
        }

        try (var in = file.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }

        return storedName;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    public boolean shouldLockByEmail(String email) {
    LocalDateTime after = LocalDateTime.now().minusMinutes(bruteForceProperties.getWindowMinutes());
    long emailFails = loginAttemptRepository
            .countByEmailAndSuccessfulIsFalseAndAttemptTimeAfter(normalizeEmail(email), after);
    return emailFails >= bruteForceProperties.getMaxEmailAttempts();
}


    private String mimeToExtension(String mime) {
        return switch (mime) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> "";
        };
    }

    private String randomSuffix() {
        return Long.toString(Double.doubleToLongBits(Math.random())).substring(0, 6);
    }
}
