package com.secdev.project;

import com.secdev.project.config.BruteForceProperties;
import com.secdev.project.repo.LoginAttemptRepository;
import com.secdev.project.repo.UserRepository;
import com.secdev.project.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;

@ExtendWith(MockitoExtension.class)
public abstract class TestBase {

    @Mock protected UserRepository userRepository;
    @Mock protected LoginAttemptRepository loginAttemptRepository;
    @Mock protected PasswordEncoder passwordEncoder;
    @Mock protected BruteForceProperties bruteForceProperties;

    @InjectMocks protected UserService userService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void baseSetup() {
        ReflectionTestUtils.setField(userService, "uploadDir", tempDir.toString());
    }
}
