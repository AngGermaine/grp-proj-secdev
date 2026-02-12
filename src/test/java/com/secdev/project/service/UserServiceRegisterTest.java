package com.secdev.project.service;
import com.secdev.project.TestBase;

import com.secdev.project.dto.RegisterRequest;
import com.secdev.project.model.User;
import com.secdev.project.service.exceptions.BadRequestException;
import com.secdev.project.util.TestDataFactory;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceRegisterTest extends TestBase {

    @Test
    void register_success_withoutPhoto() throws Exception {

        RegisterRequest req = TestDataFactory.validRegisterRequest();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.register(req, null);

        assertNotNull(result);
        assertEquals("hashedPassword", result.getPassword());
        assertNull(result.getProfilePhotoPath());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldFail_ifEmailExists() {

        RegisterRequest req = TestDataFactory.validRegisterRequest();

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> userService.register(req, null));

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldHashPassword() throws Exception {

        RegisterRequest req = TestDataFactory.validRegisterRequest();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("bcryptHash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.register(req, null);

        assertNotEquals(req.getPassword(), saved.getPassword());
        assertEquals("bcryptHash", saved.getPassword());
    }

    @Test
    void register_withValidImage_shouldStoreProfilePhoto() throws Exception {

        RegisterRequest req = TestDataFactory.validRegisterRequest();

        MockMultipartFile file = new MockMultipartFile(
                "profilePhoto",
                "photo.jpg",
                "image/jpeg",
                new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF} 
        );

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.register(req, file);

        assertNotNull(saved.getProfilePhotoPath());
        assertTrue(saved.getProfilePhotoPath().endsWith(".jpg"));
    }
}
