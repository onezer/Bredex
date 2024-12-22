package com.bredex.car.security;

import com.bredex.car.service.LogService;
import com.bredex.car.service.TokenService;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class JwtServiceTest {

    @Mock
    private LogService logService;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private JwtService jwtService;

    @Value("${security.jwt.secret-key}")
    private String jwtSecret;

    @Value("${security.jwt.access-token-expiration}")
    private long jwtAccessTokenExpirationInMs;

    @Value("${security.jwt.refresh-token-expiration}")
    private long jwtRefreshTokenExpirationInMs;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtService = new JwtService(logService, tokenService);
        jwtService.setJwtSecret(jwtSecret);
    }

    private SecretKey getTestSignInKey() {
        byte[] bytes = Base64.getDecoder().decode(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Test
    void testGenerateAccessToken() {
        String username = "testuser";

        doNothing().when(tokenService).saveToken(anyString(), eq(username));

        String token = jwtService.generateAccessToken(username);

        assertNotNull(token);
        verify(tokenService, times(1)).saveToken(anyString(), eq(username));
    }

    @Test
    void testGenerateRefreshToken() {
        String username = "testuser";

        doNothing().when(tokenService).saveToken(anyString(), eq(username));

        String token = jwtService.generateRefreshToken(username);

        assertNotNull(token);
        verify(tokenService, times(1)).saveToken(anyString(), eq(username));
    }

    @Test
    void testGetUsernameFromToken() {
        String username = "testuser";
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtAccessTokenExpirationInMs);

        String token = Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getTestSignInKey())
                .compact();

        String result = jwtService.getUsernameFromToken(token);

        assertEquals(username, result);
    }

    @Test
    void testValidateToken_ValidToken() {
        String username = "testuser";
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtAccessTokenExpirationInMs);

        String token = Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getTestSignInKey())
                .compact();

        when(tokenService.tokenExists(token)).thenReturn(true);
        when(logService.isUserLoggedOut(username)).thenReturn(false);

        boolean isValid = jwtService.validateToken(token);

        assertTrue(isValid);
        verify(tokenService, times(1)).tokenExists(token);
    }

    @Test
    void testValidateToken_ExpiredToken() {
        String username = "testuser";
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() - 1000); // Already expired

        String token = Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getTestSignInKey())
                .compact();

        boolean isValid = jwtService.validateToken(token);

        assertFalse(isValid);
    }

    @Test
    void testIsAccessToken() {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtAccessTokenExpirationInMs);

        String token = Jwts.builder()
                .subject("testuser")
                .claims(Map.of("isAccessToken", true))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getTestSignInKey())
                .compact();

        boolean isAccessToken = jwtService.isAccessToken(token);

        assertTrue(isAccessToken);
    }

    @Test
    void testSanityCheck_UserLoggedOut() {
        String username = "testuser";
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtAccessTokenExpirationInMs);

        String token = Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getTestSignInKey())
                .compact();

        when(logService.isUserLoggedOut(username)).thenReturn(true);

        boolean sanityCheck = jwtService.sanityCheck(token);

        assertFalse(sanityCheck);
        verify(logService, times(1)).isUserLoggedOut(username);
    }
}
