package com.album.seplag.config;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtConfig {

    private static final String CLAIM_ROLES_ACCESS = "roles_access";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_REFRESH = "refresh";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String username, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        var builder = Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate);

        if (roles != null && !roles.isEmpty()) {
            builder.claim(CLAIM_ROLES_ACCESS, roles);
        }

        return builder.signWith(getSigningKey()).compact();
    }

    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateToken(String username) {
        return generateAccessToken(username, List.of());
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public Boolean validateToken(String token, String username) {
        final String tokenUsername = getUsernameFromToken(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }

    public List<String> getRolesFromToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            Object rolesObj = claims.get(CLAIM_ROLES_ACCESS);
            if (rolesObj instanceof List<?> list) {
                List<String> roles = new ArrayList<>();
                for (Object item : list) {
                    if (item instanceof String s) {
                        roles.add(s);
                    }
                }
                return roles;
            }
        } catch (Exception ignored) {
        }
        return List.of();
    }

    public boolean isRefreshToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return TYPE_REFRESH.equals(claims.get(CLAIM_TYPE));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token, String username) {
        if (!isRefreshToken(token)) {
            return false;
        }
        final String tokenUsername = getUsernameFromToken(token);
        return tokenUsername.equals(username) && !isTokenExpired(token);
    }

    public Long getExpiration() {
        return expiration;
    }
}