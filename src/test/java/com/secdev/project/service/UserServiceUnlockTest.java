package com.secdev.project.service;
import com.secdev.project.TestBase;

import com.secdev.project.model.User;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceUnlockTest extends TestBase {

    @Test
    void lockAccount_shouldSetLockTime() {

        User user = new User();
        user.setAccountNonLocked(true);

        when(userRepository.findByEmail(anyString())).thenReturn(java.util.Optional.of(user));

        userService.lockAccount("test@mail.com");

        assertFalse(user.isAccountNonLocked());
        assertNotNull(user.getLockTime());

        verify(userRepository).save(user);
    }

    @Test
    void checkAndUnlockIfExpired_shouldUnlockAfterTime() {

        when(bruteForceProperties.getLockMinutes()).thenReturn(30);

        User user = new User();
        user.setAccountNonLocked(false);
        user.setLockTime(LocalDateTime.now().minusMinutes(40));

        userService.checkAndUnlockIfExpired(user);

        assertTrue(user.isAccountNonLocked());
        assertNull(user.getLockTime());
        verify(userRepository).save(user);
    }

    @Test
    void checkAndUnlockIfExpired_shouldNotUnlockEarly() {

        when(bruteForceProperties.getLockMinutes()).thenReturn(30);

        User user = new User();
        user.setAccountNonLocked(false);
        user.setLockTime(LocalDateTime.now().minusMinutes(10));

        userService.checkAndUnlockIfExpired(user);

        assertFalse(user.isAccountNonLocked());
        verify(userRepository, never()).save(any());
    }
}
