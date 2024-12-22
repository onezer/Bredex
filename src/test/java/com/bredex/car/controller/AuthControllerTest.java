package com.bredex.car.controller;

import com.bredex.car.model.*;
import com.bredex.car.security.JwtService;
import com.bredex.car.service.AuthenticationService;
import com.bredex.car.service.TokenService;
import com.bredex.car.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegister_Success() {
        UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO("testuser", "Password1", "test@example.com");

        when(userService.isValidPassword(userRegistrationDTO.getPassword())).thenReturn(true);
        when(userService.isValidUsername(userRegistrationDTO.getUsername())).thenReturn(true);
        when(userService.isValidEmail(userRegistrationDTO.getEmail())).thenReturn(true);
        when(userService.isEmailAlreadyRegistered(userRegistrationDTO.getEmail())).thenReturn(false);
        when(userService.isUsernameAlreadyRegistered(userRegistrationDTO.getUsername())).thenReturn(false);
        when(authenticationService.registerUser(userRegistrationDTO)).thenReturn(1L);
        when(userService.getById(1L)).thenReturn(Optional.of(new UserDTO(1L,"testuser", "Password1", "test@example.com")));

        ResponseEntity<?> response = authController.register(userRegistrationDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody() instanceof SuccessResponse);
        assertEquals("Registration successful", ((SuccessResponse) response.getBody()).getMessage());
    }

    @Test
    void testRegister_InvalidPassword() {
        UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO("testuser", "short", "test@example.com");

        when(userService.isValidPassword(userRegistrationDTO.getPassword())).thenReturn(false);

        ResponseEntity<?> response = authController.register(userRegistrationDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
        assertTrue(((ErrorResponse) response.getBody()).getDetails().contains("Invalid password: it must be at least 8 characters long, contain uppercase, lowercase letters and numbers!"));
    }

    @Test
    void testLogin_Success() {
        UserLoginDTO userLoginDTO = new UserLoginDTO("testuser", "Password1");
        UserDTO userDTO = new UserDTO(1L, "testuser", "Password1", "test@example.com");

        when(authenticationService.login(userLoginDTO)).thenReturn(userDTO);
        when(jwtService.generateAccessToken(userLoginDTO.getUsername())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(userLoginDTO.getUsername())).thenReturn("refresh-token");

        ResponseEntity<?> response = authController.login(userLoginDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, String> tokens = (Map<String, String>) response.getBody();
        assertEquals("access-token", tokens.get("access-token"));
        assertEquals("refresh-token", tokens.get("refresh-token"));
    }

    @Test
    void testLogin_InvalidCredentials() {
        UserLoginDTO userLoginDTO = new UserLoginDTO("testuser", "WrongPassword");

        when(authenticationService.login(userLoginDTO)).thenThrow(new RuntimeException("Invalid login credentials!"));

        ResponseEntity<?> response = authController.login(userLoginDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
        assertEquals("Invalid login credentials!", ((ErrorResponse) response.getBody()).getMessage());
    }

    @Test
    void testRefresh_Success() {
        String refreshToken = "refresh-token";
        String authHeader = "Bearer " + refreshToken;

        when(jwtService.validateToken(refreshToken)).thenReturn(true);
        when(jwtService.isAccessToken(refreshToken)).thenReturn(false);
        when(jwtService.getUsernameFromToken(refreshToken)).thenReturn("testuser");
        when(jwtService.generateAccessToken("testuser")).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken("testuser")).thenReturn("new-refresh-token");

        ResponseEntity<?> response = authController.refresh(authHeader);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, String> tokens = (Map<String, String>) response.getBody();
        assertEquals("new-access-token", tokens.get("access-token"));
        assertEquals("new-refresh-token", tokens.get("refresh-token"));
    }

    @Test
    void testRefresh_InvalidToken() {
        String refreshToken = "invalid-refresh-token";
        String authHeader = "Bearer " + refreshToken;

        when(jwtService.validateToken(refreshToken)).thenReturn(false);

        ResponseEntity<?> response = authController.refresh(authHeader);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("", response.getBody());
    }

    @Test
    void testLogout_Success() {
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(authentication.getName()).thenReturn("testuser");
        doNothing().when(authenticationService).logout("testuser");

        ResponseEntity<?> response = authController.logout();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof SuccessResponse);
        assertEquals("Logged out", ((SuccessResponse) response.getBody()).getMessage());
    }
}
