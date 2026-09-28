package com.phuoc.exception;

/** JWT da qua han (claim exp). */
public class JwtExpiredException extends InvalidJwtException {
    private static final long serialVersionUID = 1L;

    public JwtExpiredException(String message) {
        super(message);
    }
}
