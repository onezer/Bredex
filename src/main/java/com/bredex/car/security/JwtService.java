package com.bredex.car.security;

import com.bredex.car.service.LogService;
import com.bredex.car.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
@Slf4j
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String jwtSecret;

    @Value("${security.jwt.access-token-expiration}")
    private long jwtAccessTokenExpirationInMs;

    @Value("${security.jwt.refresh-token-expiration}")
    private long jwtRefreshTokenExpirationInMs;

    private final LogService logService;
    private final TokenService tokenService;

    void setJwtSecret(String secret) {
        jwtSecret = secret;
    }

    public JwtService(LogService logService, TokenService tokenService) {
        this.logService = logService;
        this.tokenService = tokenService;
    }

    public String generateAccessToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtAccessTokenExpirationInMs);

        String token = Jwts.builder()
                .subject(username)
                .claims(Map.of("isAccessToken", true))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSignInKey())
                .compact();

        tokenService.saveToken(token, username);

        return token;
    }

    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtRefreshTokenExpirationInMs);

        String token = Jwts.builder()
                        .subject(username)
                        .claims(Map.of("isAccessToken", false))
                        .issuedAt(now)
                        .expiration(expiryDate)
                        .signWith(getSignInKey())
                        .compact();

        tokenService.saveToken(token, username);

        return token;
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getExpiration().after(new Date()) && sanityCheck(token) && tokenService.tokenExists(token);

        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return (boolean)claims.get("isAccessToken");
    }

    boolean sanityCheck(String token) {
        String username = getUsernameFromToken(token);
        try {
            return !logService.isUserLoggedOut(username);
        }
        catch (UsernameNotFoundException e) {
            log.error("Token's username: {} does not exist in the database as a registered user! Token: {}", username, token);
            return false;
        }
    }

    private SecretKey getSignInKey() {
        byte[] bytes = Base64.getDecoder().decode(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(bytes, "HmacSHA256"); }
}
