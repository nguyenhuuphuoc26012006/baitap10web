package com.phuoc.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.phuoc.entity.User;
import com.phuoc.exception.InvalidJwtException;
import com.phuoc.exception.JwtExpiredException;
import com.phuoc.exception.JwtSignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** Kiem thu JwtService (Nimbus) - khong can Spring context, khong can MySQL. */
class JwtServiceTest {

    private static final String SECRET_A = "I3r+GfxVpBefyRSYefdITerLMADToD0TV5YKTYKugMo=";
    private static final String SECRET_B = "1ba7QTm1zv4al0aSkPH01MymJoMLY5dDMk6Z0sPq91o=";

    private JwtService service;
    private User alice;

    @BeforeEach
    void setUp() {
        service = newService(SECRET_A, 3_600_000L);
        alice = user("alice@phuoc.com");
    }

    private static JwtService newService(String secret, long expirationMs) {
        JwtService s = new JwtService();
        ReflectionTestUtils.setField(s, "secretKey", secret);
        ReflectionTestUtils.setField(s, "jwtExpiration", expirationMs);
        return s;
    }

    private static User user(String email) {
        User u = new User();
        u.setEmail(email);
        return u;
    }

    @Test
    void tokenHasThreePartsAndRoundTrips() {
        String token = service.generateToken(alice);

        assertEquals(3, token.split("\\.").length); // header.payload.signature
        assertEquals("alice@phuoc.com", service.extractUsername(token));
        assertTrue(service.isTokenValid(token, alice));
        assertFalse(service.isTokenValid(token, user("bob@phuoc.com")));
    }

    @Test
    void tamperedPayloadIsRejected() {
        String[] a = service.generateToken(alice).split("\\.");
        String[] b = service.generateToken(user("bob@phuoc.com")).split("\\.");
        // payload cua bob + chu ky cua alice
        String forged = b[0] + "." + b[1] + "." + a[2];

        assertThrows(JwtSignatureException.class, () -> service.extractUsername(forged));
    }

    @Test
    void tokenSignedWithOtherSecretIsRejected() {
        String token = newService(SECRET_B, 3_600_000L).generateToken(alice);

        assertThrows(JwtSignatureException.class, () -> service.extractUsername(token));
    }

    @Test
    void expiredTokenIsRejected() {
        String token = newService(SECRET_A, -1_000L).generateToken(alice);

        assertThrows(JwtExpiredException.class, () -> service.extractUsername(token));
    }

    @Test
    void malformedTokenIsRejected() {
        assertThrows(InvalidJwtException.class, () -> service.extractUsername("dfdfdfdfd"));
    }

    @Test
    void shortSecretIsRejected() {
        JwtService weak = newService("YWJj", 3_600_000L); // "abc" -> 3 byte
        assertThrows(IllegalStateException.class, () -> weak.generateToken(alice));
    }
}
