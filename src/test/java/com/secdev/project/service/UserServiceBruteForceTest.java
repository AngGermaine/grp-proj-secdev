package com.secdev.project.service;
import com.secdev.project.TestBase;

import com.secdev.project.service.exceptions.TooManyAttemptsException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceBruteForceTest extends TestBase {

    @Test
    void assertNotBlocked_shouldThrow_whenMaxEmailReached() {

        when(bruteForceProperties.getWindowMinutes()).thenReturn(10);
        when(bruteForceProperties.getMaxEmailAttempts()).thenReturn(3);

        when(loginAttemptRepository
                .countByEmailAndSuccessfulIsFalseAndAttemptTimeAfter(anyString(), any()))
                .thenReturn(3L);

        assertThrows(TooManyAttemptsException.class,
                () -> userService.assertNotBlocked("test@mail.com", "127.0.0.1"));
    }


    @Test
    void shouldLockByEmail_returnsTrue_whenThresholdReached() {

        when(bruteForceProperties.getWindowMinutes()).thenReturn(10);
        when(bruteForceProperties.getMaxEmailAttempts()).thenReturn(3);

        when(loginAttemptRepository
                .countByEmailAndSuccessfulIsFalseAndAttemptTimeAfter(anyString(), any()))
                .thenReturn(3L);

        boolean result = userService.shouldLockByEmail("test@mail.com");

        assertTrue(result);
    }
}
