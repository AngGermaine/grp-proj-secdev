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

import jakarta.annotation.PostConstruct;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class UserService {     
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private static final long MAX_PHOTO_BYTES = 5L * 1024 * 1024; 
    private static final Set<String> ALLOWED_IMAGE_MIME = Set.of("image/jpeg", "image/png", "image/webp");

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?\\d{8,15}$");

    private final UserRepository userRepository;
    private final BruteForceProperties bruteForceProperties;
    private final LoginAttemptRepository loginAttemptRepository;
    private final PasswordEncoder passwordEncoder;
    private final Tika tika = new Tika();

    @Value("${file.upload-dir:src/main/resources/static/uploads/}")
    private String uploadDir;

    @Value("${security.admin.password:AdminChangeMe123!}")
    private String defaultAdminPassword;

    public UserService(UserRepository userRepository,
                       LoginAttemptRepository loginAttemptRepository,
                       @Lazy PasswordEncoder passwordEncoder,
                       BruteForceProperties bruteForceProperties) {
        this.userRepository = userRepository;
        this.loginAttemptRepository = loginAttemptRepository;
        this.passwordEncoder = passwordEncoder;
        this.bruteForceProperties = bruteForceProperties;
    }


    @PostConstruct
    public void initDefaultAdmin() {
        if (userRepository.findByEmail("admin@secdev.com").isEmpty()) {
            User admin = new User();
            admin.setFullName("Default Administrator");
            admin.setEmail("admin@secdev.com");
            admin.setPhoneNumber("+63912345678");
            admin.setPassword(passwordEncoder.encode(defaultAdminPassword)); 
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            admin.setAccountNonLocked(true);
            userRepository.save(admin);
            
            logger.info("Default Admin Account Created: admin@secdev.com");
        }
    }

    @Transactional
    public User register(RegisterRequest req, MultipartFile profilePhoto) throws IOException {
        validateInputs(req);

        String email = normalizeEmail(req.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already in use.");
        }

        User user = new User();
        user.setFullName(req.getFullName().trim());
        user.setEmail(email);
        user.setPhoneNumber(req.getPhoneNumber().trim());
        user.setRole(Role.USER);
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        user.setPassword(passwordEncoder.encode(req.getPassword()));

        if (profilePhoto != null && !profilePhoto.isEmpty()) {
            String storedFileName = storeProfilePhoto(profilePhoto);
            user.setProfilePhotoPath("/uploads/" + storedFileName);
        }

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("Registration failed due to data constraints.");
        }
    }

    private void validateInputs(RegisterRequest req) {
        if (req.getFullName() == null || req.getFullName().isBlank()) 
            throw new BadRequestException("Full Name is required.");
        
        if (!EMAIL_PATTERN.matcher(req.getEmail()).matches()) 
            throw new BadRequestException("Invalid email format.");
        
        if (!PHONE_PATTERN.matcher(req.getPhoneNumber()).matches()) 
            throw new BadRequestException("Invalid phone number format (8-15 digits).");
            
        if (req.getPassword() == null || req.getPassword().length() < 8)
            throw new BadRequestException("Password must be at least 8 characters.");
    }

    @Transactional
    public void lockAccount(String email) {
        userRepository.findByEmail(normalizeEmail(email)).ifPresent(u -> {
            if (u.isAccountNonLocked()) {
                u.setAccountNonLocked(false);
                u.setLockTime(LocalDateTime.now());
                userRepository.save(u);
                logger.warn("Account locked: {}", email);
            }
        });
    }

    @Transactional
    public void checkAndUnlockIfExpired(User user) {
        if (user.isAccountNonLocked() || user.getLockTime() == null) return;

        LocalDateTime unlockTime = user.getLockTime().plusMinutes(bruteForceProperties.getLockMinutes());
        if (LocalDateTime.now().isAfter(unlockTime)) {
            user.setAccountNonLocked(true);
            user.setLockTime(null);
            userRepository.save(user);
            logger.info("Account unlocked: {}", user.getEmail());
        }
    }

    public void assertNotBlocked(String email, String ipAddress) {
        if (email == null || email.isBlank()) return;

        LocalDateTime after = LocalDateTime.now().minusMinutes(bruteForceProperties.getWindowMinutes());
        String normalizedEmail = normalizeEmail(email);

        long emailFails = loginAttemptRepository
                .countByEmailAndSuccessfulIsFalseAndAttemptTimeAfter(normalizedEmail, after);

        long ipFails = loginAttemptRepository
                .countByIpAddressAndSuccessfulIsFalseAndAttemptTimeAfter(ipAddress, after);

        if (emailFails >= bruteForceProperties.getMaxEmailAttempts() || 
            ipFails >= bruteForceProperties.getMaxIpAttempts()) {
            logger.warn("Blocking login attempt for {} from IP {}", email, ipAddress);
            throw new TooManyAttemptsException("Too many login attempts. Try again later.");
        }
    }

    public void recordLoginAttempt(String email, boolean success, String ipAddress) {
        LoginAttempt attempt = new LoginAttempt(normalizeEmail(email), success, ipAddress);
        attempt.setAttemptTime(LocalDateTime.now());
        loginAttemptRepository.save(attempt);
    }

    public boolean shouldLockByEmail(String email) {
        LocalDateTime after = LocalDateTime.now().minusMinutes(bruteForceProperties.getWindowMinutes());
        long emailFails = loginAttemptRepository.countByEmailAndSuccessfulIsFalseAndAttemptTimeAfter(normalizeEmail(email), after);
        return emailFails >= bruteForceProperties.getMaxEmailAttempts();
    }

    private String storeProfilePhoto(MultipartFile file) throws IOException {
        if (file.getSize() > MAX_PHOTO_BYTES) throw new BadRequestException("Photo exceeds 5MB limit.");

        String mime = tika.detect(file.getInputStream());
        if (!ALLOWED_IMAGE_MIME.contains(mime)) {
            throw new BadRequestException("Invalid file type. Only JPG, PNG, and WEBP are allowed.");
        }

        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(dir)) Files.createDirectories(dir);

        String ext = mimeToExtension(mime);
        String storedName = "user_" + System.currentTimeMillis() + ext;
        Path dest = dir.resolve(storedName);

        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
        return storedName;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(normalizeEmail(email));
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String mimeToExtension(String mime) {
        return switch (mime) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".bin";
        };
    }
}