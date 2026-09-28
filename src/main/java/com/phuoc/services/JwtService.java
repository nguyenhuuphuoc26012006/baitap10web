package com.phuoc.services;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.phuoc.exception.InvalidJwtException;
import com.phuoc.exception.JwtExpiredException;
import com.phuoc.exception.JwtSignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Buoc 4: JwtService - tao va kiem tra JWT bang thu vien Nimbus JOSE+JWT (HS256).
 *
 * Doi chieu voi jjwt trong slide:
 *  Jwts.builder()...signWith(key, HS256).compact()  ->  new SignedJWT(header, claims) + sign(new MACSigner(key)) + serialize()
 *  Jwts.parser().verifyWith(key).parseSignedClaims  ->  SignedJWT.parse(token) + verify(new MACVerifier(key)) + getJWTClaimsSet()
 *  SignatureException / ExpiredJwtException         ->  JwtSignatureException / JwtExpiredException (tu dinh nghia)
 */
@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, JWTClaimsSet::getSubject);
    }

    public <T> T extractClaim(String token, Function<JWTClaimsSet, T> claimsResolver) {
        final JWTClaimsSet claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        Date now = new Date();

        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder();
        extraClaims.forEach(claims::claim);
        claims.subject(userDetails.getUsername())
                .issueTime(now)
                .expirationTime(new Date(now.getTime() + expiration));

        // header: {"typ":"JWT","alg":"HS256"}
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256)
                .type(JOSEObjectType.JWT)
                .build();

        SignedJWT jwt = new SignedJWT(header, claims.build());
        try {
            jwt.sign(new MACSigner(getSignInKey()));
        } catch (JOSEException e) {
            throw new IllegalStateException("Khong the ky JWT (kiem tra security.jwt.secret-key >= 32 byte)", e);
        }
        return jwt.serialize(); // header.payload.signature
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, JWTClaimsSet::getExpirationTime);
    }

    /**
     * Parse + kiem tra chu ky + kiem tra han su dung. Nem:
     *  - InvalidJwtException   : chuoi khong phai JWT / sai thuat toan
     *  - JwtSignatureException : sai chu ky
     *  - JwtExpiredException   : het han
     */
    private JWTClaimsSet extractAllClaims(String token) {
        final SignedJWT jwt;
        try {
            jwt = SignedJWT.parse(token);
        } catch (ParseException e) {
            throw new InvalidJwtException("The JWT is malformed", e);
        }

        // Chi chap nhan HS256 (chan token doi alg de qua mat)
        if (!JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm())) {
            throw new InvalidJwtException("Unsupported JWT algorithm");
        }

        try {
            if (!jwt.verify(new MACVerifier(getSignInKey()))) {
                throw new JwtSignatureException("The JWT signature is invalid");
            }
        } catch (JOSEException e) {
            throw new JwtSignatureException("The JWT signature cannot be verified", e);
        }

        final JWTClaimsSet claims;
        try {
            claims = jwt.getJWTClaimsSet();
        } catch (ParseException e) {
            throw new InvalidJwtException("The JWT claims are malformed", e);
        }

        Date exp = claims.getExpirationTime();
        if (exp == null) {
            throw new InvalidJwtException("The JWT has no expiration time");
        }
        if (exp.before(new Date())) {
            throw new JwtExpiredException("The JWT token has expired");
        }
        return claims;
    }

    /** secret-key (Base64) -> mang byte dung lam khoa HMAC. HS256 can >= 256 bit = 32 byte. */
    private byte[] getSignInKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey.trim());
        if (keyBytes.length < 32) {
            throw new IllegalStateException("security.jwt.secret-key phai dai it nhat 32 byte (256 bit) sau khi giai ma Base64");
        }
        return keyBytes;
    }
}
