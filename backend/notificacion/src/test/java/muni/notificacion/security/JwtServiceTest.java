package muni.notificacion.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secretKey = "bXVuaS1kaWdpdGFsLXNlY3JldC1rZXktMjAyNC1zZWN1cmUtand0LXRva2Vu";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
    }

    private String generateToken(String username, String role, long expirationMs) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
                .subject(username)
                .claim("rol", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    @Test
    void testExtractUsername() {
        String token = generateToken("12345678-9", "ADMIN", 1000 * 60);
        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("12345678-9");
    }

    @Test
    void testExtractRol() {
        String token = generateToken("12345678-9", "ADMIN", 1000 * 60);
        String role = jwtService.extractRol(token);
        assertThat(role).isEqualTo("ADMIN");
    }

    @Test
    void testIsTokenValid_Success() {
        String token = generateToken("12345678-9", "FUNCIONARIO", 1000 * 60);
        boolean isValid = jwtService.isTokenValid(token);
        assertThat(isValid).isTrue();
    }

    @Test
    void testIsTokenValid_Expired() {
        // Token expired 1 minute ago
        String token = generateToken("12345678-9", "VECINO", -1000 * 60);
        boolean isValid = jwtService.isTokenValid(token);
        assertThat(isValid).isFalse();
    }

    @Test
    void testIsTokenValid_Corrupted() {
        boolean isValid = jwtService.isTokenValid("invalid.token.signature");
        assertThat(isValid).isFalse();
    }

    @Test
    void testIsTokenValid_ExpiredNoException() {
        JwtService spyService = org.mockito.Mockito.spy(jwtService);
        org.mockito.Mockito.doReturn(true).when(spyService).isTokenExpired(org.mockito.Mockito.anyString());
        boolean isValid = spyService.isTokenValid("any-token-string");
        assertThat(isValid).isFalse();
    }
}
