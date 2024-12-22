package com.bredex.car.service;

import com.bredex.car.model.*;
import com.bredex.car.repository.LogRepository;
import com.bredex.car.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private TokenService tokenService;

    @Mock
    private LogRepository logRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser_Success() {
        UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO();
        userRegistrationDTO.setUsername("testuser");
        userRegistrationDTO.setPassword("password123");
        userRegistrationDTO.setEmail("testuser@example.com");

        UserDTO savedUser = new UserDTO();
        savedUser.setId(1L);

        when(passwordEncoder.encode(userRegistrationDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserDTO.class))).thenReturn(savedUser);

        Long userId = authenticationService.registerUser(userRegistrationDTO);

        assertEquals(1L, userId);
        verify(userRepository, times(1)).save(any(UserDTO.class));
        verify(passwordEncoder, times(1)).encode("password123");
    }

    @Test
    void testLogin_Success() {
        UserLoginDTO userLoginDTO = new UserLoginDTO();
        userLoginDTO.setUsername("testuser");
        userLoginDTO.setPassword("password123");

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setPassword("encodedPassword");
        userDTO.setEmail("test@test.com");
        userDTO.setId(1L);

        when(userService.getByUsername("testuser")).thenReturn(Optional.of(userDTO));

        LogDTO logDTO = new LogDTO();
        logDTO.setType("login");
        logDTO.setTimestamp(new Date());
        logDTO.setUser(userDTO);
        logDTO.setId(1L);

        when(logRepository.save(any(LogDTO.class))).thenReturn(logDTO);

        UserDTO result = authenticationService.login(userLoginDTO);

        assertEquals("testuser", result.getUsername());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(logRepository, times(1)).save(any(LogDTO.class));
    }

    @Test
    void testLogin_InvalidCredentials() {
        UserLoginDTO userLoginDTO = new UserLoginDTO();
        userLoginDTO.setUsername("testuser");
        userLoginDTO.setPassword("wrongpassword");

        doThrow(new BadCredentialsException("Invalid credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(BadCredentialsException.class, () -> authenticationService.login(userLoginDTO));
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(logRepository, never()).save(any(LogDTO.class));
    }

    @Test
    void testLogout_Success() {
        String username = "testuser";

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(username);

        when(userService.getByUsername(username)).thenReturn(Optional.of(userDTO));

        LogDTO logDTO = new LogDTO();
        logDTO.setType("login");
        logDTO.setTimestamp(new Date());
        logDTO.setUser(userDTO);

        when(logRepository.save(any(LogDTO.class))).thenReturn(logDTO);

        doNothing().when(tokenService).deleteTokensOfUser(username);

        authenticationService.logout(username);

        verify(userService, times(1)).getByUsername(username);
        verify(logRepository, times(1)).save(any(LogDTO.class));
        verify(tokenService, times(1)).deleteTokensOfUser(username);
    }

    @Test
    void testLogout_UserNotFound() {
        String username = "nonexistent";

        when(userService.getByUsername(username)).thenReturn(Optional.empty());

        authenticationService.logout(username);

        verify(userService, times(1)).getByUsername(username);
        verify(logRepository, never()).save(any(LogDTO.class));
        verify(tokenService, never()).deleteTokensOfUser(username);
    }
}
