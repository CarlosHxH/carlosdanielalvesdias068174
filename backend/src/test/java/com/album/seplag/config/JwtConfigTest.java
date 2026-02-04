package com.album.seplag.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtConfigTest {

    private JwtConfig jwtConfig;

    @BeforeEach
    void setUp() {
        jwtConfig = new JwtConfig();
        ReflectionTestUtils.setField(jwtConfig, "secret", "test-secret-key-for-testing-only-32chars");
        ReflectionTestUtils.setField(jwtConfig, "expiration", 300000L);
        ReflectionTestUtils.setField(jwtConfig, "refreshExpiration", 604800000L);
    }

    @Test
    void generateAccessToken_ShouldContainRoles_WhenRolesProvided() {
        String token = jwtConfig.generateAccessToken("testuser", List.of("ROLE_USER", "ROLE_ADMIN"));

        assertNotNull(token);
        assertEquals("testuser", jwtConfig.getUsernameFromToken(token));
        List<String> roles = jwtConfig.getRolesFromToken(token);
        assertEquals(2, roles.size());
        assertTrue(roles.contains("ROLE_USER"));
        assertTrue(roles.contains("ROLE_ADMIN"));
        assertFalse(jwtConfig.isRefreshToken(token));
    }

    @Test
    void generateAccessToken_ShouldWork_WhenRolesEmpty() {
        String token = jwtConfig.generateAccessToken("testuser", List.of());

        assertNotNull(token);
        assertEquals("testuser", jwtConfig.getUsernameFromToken(token));
        assertTrue(jwtConfig.getRolesFromToken(token).isEmpty());
    }

    @Test
    void generateRefreshToken_ShouldHaveTypeRefresh() {
        String token = jwtConfig.generateRefreshToken("testuser");

        assertNotNull(token);
        assertEquals("testuser", jwtConfig.getUsernameFromToken(token));
        assertTrue(jwtConfig.isRefreshToken(token));
    }

    @Test
    void getRolesFromToken_ShouldReturnEmpty_ForRefreshToken() {
        String token = jwtConfig.generateRefreshToken("testuser");

        List<String> roles = jwtConfig.getRolesFromToken(token);
        assertTrue(roles.isEmpty());
    }

    @Test
    void validateRefreshToken_ShouldReturnTrue_WhenValid() {
        String token = jwtConfig.generateRefreshToken("testuser");

        assertTrue(jwtConfig.validateRefreshToken(token, "testuser"));
    }

    @Test
    void validateRefreshToken_ShouldReturnFalse_WhenAccessToken() {
        String token = jwtConfig.generateAccessToken("testuser", List.of("ROLE_USER"));

        assertFalse(jwtConfig.validateRefreshToken(token, "testuser"));
    }

    @Test
    void validateRefreshToken_ShouldReturnFalse_WhenWrongUsername() {
        String token = jwtConfig.generateRefreshToken("testuser");

        assertFalse(jwtConfig.validateRefreshToken(token, "otheruser"));
    }
}
