package com.bredex.car.controller;

import com.bredex.car.model.*;
import com.bredex.car.security.JwtService;
import com.bredex.car.service.AuthenticationService;
import com.bredex.car.service.TokenService;
import com.bredex.car.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final UserService userService;
    private final TokenService tokenService;

    public AuthController(AuthenticationService authenticationService, JwtService jwtService, UserService userService, TokenService tokenService) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @Operation(summary = "Signs the user up, with given credentials in the request body")
    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody UserRegistrationDTO userRegistrationDTO) {
        if (userRegistrationDTO == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid request body!"));
        }

        List<String> errorMessages = new ArrayList<>();

        if(!userService.isValidPassword(userRegistrationDTO.getPassword())) {
            errorMessages.add("Invalid password: it must be at least 8 characters long, contain uppercase, lowercase letters and numbers!");
        }
        if(!userService.isValidUsername(userRegistrationDTO.getUsername())) {
            errorMessages.add("Invalid username: it must only contain letters, numbers, underscores, and hyphens!");
        }
        if(!userService.isValidEmail(userRegistrationDTO.getEmail())) {
            errorMessages.add("Invalid e-mail format!");
        }
        if(userService.isEmailAlreadyRegistered(userRegistrationDTO.getEmail())) {
            errorMessages.add("E-mail already registered, please choose another one.");
        }
        if(userService.isUsernameAlreadyRegistered(userRegistrationDTO.getUsername())) {
            errorMessages.add("Username already registered, please choose another one.");
        }

        if (!errorMessages.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse(errorMessages));
        }

        try {
            Long id = authenticationService.registerUser(userRegistrationDTO);
            log.info("User: {}, {} signed up, id: {}", userRegistrationDTO.getUsername(), userRegistrationDTO.getEmail(), id);
            log.info("psw: {}",userService.getById(id).get().getPassword());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred during registration, please try again later."));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponse("Registration successful"));

    }

    @Operation(summary = "Attempts to log in the user with the given credentials in the request body")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDTO userLoginDTO) {
        try {
            UserDTO authenticatedUser = authenticationService.login(userLoginDTO);

            log.info("User: {} logged in", userLoginDTO.getUsername());

            return ResponseEntity.ok(generateResponseTokens(authenticatedUser.getUsername()));
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid login credentials!"));
        }
    }

    @Operation(summary = "Creates new tokens for the user if the refresh token is valid")
    @GetMapping("/refresh-token")
    public ResponseEntity<?> refresh(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);

        if(!jwtService.validateToken(token) || jwtService.isAccessToken(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("");
        }

        String username = jwtService.getUsernameFromToken(token);
        Map<String, String> tokens = generateResponseTokens(username);

        log.info("User: {} refreshed tokens", username);

        return ResponseEntity.ok(tokens);
    }

    @Operation(summary = "Logs out the user")
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        authenticationService.logout(username);
        log.info("User: {} logged out", username);
        return ResponseEntity.ok(new SuccessResponse("Logged out"));
    }

    private Map<String, String> generateResponseTokens(String username) {
        tokenService.deleteTokensOfUser(username);

        String accessToken = jwtService.generateAccessToken(username);
        String refreshToken = jwtService.generateRefreshToken(username);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("access-token", accessToken);
        tokens.put("refresh-token", refreshToken);

        return tokens;
    }
}
