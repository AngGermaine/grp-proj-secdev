package com.secdev.project.service;

import com.secdev.project.TestBase;
import com.secdev.project.service.exceptions.TooManyAttemptsException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceBruteForceTest extends TestBase {

    private static final int WINDOW_MIN = 10;
    private static final int MAX_EMAIL = 3;
    private static final int MAX_IP = 20;

    private void stubEmailPolicy() {
        when(bruteForceProperties.getWindowMinutes()).thenReturn(WINDOW_MIN);
        when(bruteForceProperties.getMaxEmailAttempts()).thenReturn(MAX_EMAIL);
    }

    private void stubIpPolicy() {
        stubEmailPolicy();
        when(bruteForceProperties.getMaxIpAttempts()).thenReturn(MAX_IP);
    }

    @Test
    void assertNotBlocked_shouldThrow_whenMaxEmailReached() {
        stubEmailPolicy();

        when(loginAttemptRepository
                .countByEmailAndSuccessfulIsFalseAndAttemptTimeAfter(anyString(), any()))
                .thenReturn((long) MAX_EMAIL);

        assertThrows(TooManyAttemptsException.class,
                () -> userService.assertNotBlocked("test@mail.com", "127.0.0.1"));
    }

    @Test
    void assertNotBlocked_shouldNotThrow_whenEmailJustBelowThreshold() {
        stubIpPolicy(); 

        when(loginAttemptRepository
                .countByEmailAndSuccessfulIsFalseAndAttemptTimeAfter(anyString(), any()))
                .thenReturn((long) MAX_EMAIL - 1);

        when(loginAttemptRepository
                .countByIpAddressAndSuccessfulIsFalseAndAttemptTimeAfter(anyString(), any()))
                .thenReturn(0L);

        assertDoesNotThrow(() ->
                userService.assertNotBlocked("test@mail.com", "127.0.0.1"));
    }

    @Test
    void assertNotBlocked_shouldThrow_whenMaxIpReached() {
        stubIpPolicy();

        when(loginAttemptRepository
                .countByEmailAndSuccessfulIsFalseAndAttemptTimeAfter(anyString(), any()))
                .thenReturn(0L);

        when(loginAttemptRepository
                .countByIpAddressAndSuccessfulIsFalseAndAttemptTimeAfter(anyString(), any()))
                .thenReturn((long) MAX_IP);

        assertThrows(TooManyAttemptsException.class,
                () -> userService.assertNotBlocked("test@mail.com", "127.0.0.1"));
    }

    @Test
    void shouldLockByEmail_returnsTrue_whenThresholdReached() {
        stubEmailPolicy();

        when(loginAttemptRepository
                .countByEmailAndSuccessfulIsFalseAndAttemptTimeAfter(anyString(), any()))
                .thenReturn((long) MAX_EMAIL);

        assertTrue(userService.shouldLockByEmail("test@mail.com"));
    }

    @Test
    void shouldLockByEmail_returnsFalse_whenBelowThreshold() {
        stubEmailPolicy();

        when(loginAttemptRepository
                .countByEmailAndSuccessfulIsFalseAndAttemptTimeAfter(anyString(), any()))
                .thenReturn((long) MAX_EMAIL - 1);

        assertFalse(userService.shouldLockByEmail("test@mail.com"));
    }

    @Test
    void assertNotBlocked_shouldNormalizeEmail_toLowercase() {
        stubIpPolicy();

        when(loginAttemptRepository
                .countByEmailAndSuccessfulIsFalseAndAttemptTimeAfter(anyString(), any()))
                .thenReturn(0L);

        when(loginAttemptRepository
                .countByIpAddressAndSuccessfulIsFalseAndAttemptTimeAfter(anyString(), any()))
                .thenReturn(0L);

        userService.assertNotBlocked("TeSt@Mail.Com", "127.0.0.1");

        verify(loginAttemptRepository)
                .countByEmailAndSuccessfulIsFalseAndAttemptTimeAfter(eq("test@mail.com"), any());
    }
}
