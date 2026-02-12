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

    @Test
    void assertNotBlocked_shouldThrow_whenMaxIpReached() {

        when(bruteForceProperties.getWindowMinutes()).thenReturn(10);
        when(bruteForceProperties.getMaxEmailAttempts()).thenReturn(3);
        when(bruteForceProperties.getMaxIpAttempts()).thenReturn(20);

        when(loginAttemptRepository
                .countByEmailAndSuccessfulIsFalseAndAttemptTimeAfter(anyString(), any()))
                .thenReturn(0L);

        when(loginAttemptRepository
                .countByIpAddressAndSuccessfulIsFalseAndAttemptTimeAfter(anyString(), any()))
                .thenReturn(20L);

        assertThrows(TooManyAttemptsException.class,
                () -> userService.assertNotBlocked("test@mail.com", "127.0.0.1"));
    }

    @Test
    void assertNotBlocked_shouldThrow_whenEmailEqualsThreshold() {

        when(bruteForceProperties.getWindowMinutes()).thenReturn(10);
        when(bruteForceProperties.getMaxEmailAttempts()).thenReturn(3);

        when(loginAttemptRepository
                .countByEmailAndSuccessfulIsFalseAndAttemptTimeAfter(anyString(), any()))
                .thenReturn(3L);

        assertThrows(TooManyAttemptsException.class,
                () -> userService.assertNotBlocked("test@mail.com", "127.0.0.1"));
    }

    @Test
    void assertNotBlocked_shouldNotThrow_whenEmailJustBelowThreshold() {

        when(bruteForceProperties.getWindowMinutes()).thenReturn(10);
        when(bruteForceProperties.getMaxEmailAttempts()).thenReturn(3);
        when(bruteForceProperties.getMaxIpAttempts()).thenReturn(20);

        when(loginAttemptRepository
                .countByEmailAndSuccessfulIsFalseAndAttemptTimeAfter(anyString(), any()))
                .thenReturn(2L);

        when(loginAttemptRepository
                .countByIpAddressAndSuccessfulIsFalseAndAttemptTimeAfter(anyString(), any()))
                .thenReturn(0L);

        assertDoesNotThrow(() ->
                userService.assertNotBlocked("test@mail.com", "127.0.0.1"));
    }

    @Test
    void assertNotBlocked_shouldNormalizeEmail_toLowercase() {

        when(bruteForceProperties.getWindowMinutes()).thenReturn(10);
        when(bruteForceProperties.getMaxEmailAttempts()).thenReturn(3);
        when(bruteForceProperties.getMaxIpAttempts()).thenReturn(20);

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

    @Test
    void shouldLockByEmail_returnsFalse_whenBelowThreshold() {

        when(bruteForceProperties.getWindowMinutes()).thenReturn(10);
        when(bruteForceProperties.getMaxEmailAttempts()).thenReturn(3);

        when(loginAttemptRepository
                .countByEmailAndSuccessfulIsFalseAndAttemptTimeAfter(anyString(), any()))
                .thenReturn(2L);

        boolean result = userService.shouldLockByEmail("test@mail.com");

        assertFalse(result);
    }

}
