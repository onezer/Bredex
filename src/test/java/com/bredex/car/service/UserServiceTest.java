package com.bredex.car.service;

import com.bredex.car.model.UserDTO;
import com.bredex.car.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetById() {
        Long userId = 1L;
        UserDTO mockUser = new UserDTO();
        mockUser.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        Optional<UserDTO> result = userService.getById(userId);

        assertTrue(result.isPresent());
        assertEquals(mockUser, result.get());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testGetByUsername() {
        String username = "testuser";
        UserDTO mockUser = new UserDTO();
        mockUser.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));

        Optional<UserDTO> result = userService.getByUsername(username);

        assertTrue(result.isPresent());
        assertEquals(mockUser, result.get());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void testIsEmailAlreadyRegistered() {
        String email = "test@example.com";
        UserDTO mockUser = new UserDTO();
        mockUser.setEmail(email);
        when(userRepository.findByEmail(email.toLowerCase())).thenReturn(Optional.of(mockUser));

        boolean result = userService.isEmailAlreadyRegistered(email);

        assertTrue(result);
        verify(userRepository, times(1)).findByEmail(email.toLowerCase());
    }

    @Test
    void testIsUsernameAlreadyRegistered() {
        String username = "testuser";
        UserDTO mockUser = new UserDTO();
        mockUser.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));

        boolean result = userService.isUsernameAlreadyRegistered(username);

        assertTrue(result);
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void testIsValidEmail() {
        assertTrue(userService.isValidEmail("valid.email@example.com"));
        assertFalse(userService.isValidEmail("invalid-email"));
        assertFalse(userService.isValidEmail(null));
    }

    @Test
    void testIsValidUsername() {
        assertTrue(userService.isValidUsername("valid_username"));
        assertTrue(userService.isValidUsername("valid-username"));
        assertTrue(userService.isValidUsername("ValidUsername"));
        assertFalse(userService.isValidUsername(null));
        assertFalse(userService.isValidUsername(""));
        assertFalse(userService.isValidUsername("username#invalid"));
    }

    @Test
    void testIsValidPassword() {
        assertTrue(userService.isValidPassword("Valid123"));
        assertFalse(userService.isValidPassword("short"));
        assertFalse(userService.isValidPassword("nouppercase123"));
        assertFalse(userService.isValidPassword("NOLOWERCASE123"));
        assertFalse(userService.isValidPassword("NoDigits"));
    }
}