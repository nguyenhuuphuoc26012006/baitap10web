package com.phuoc.exception;

/** Chu ky JWT khong khop (token bi sua hoac ky bang secret khac). */
public class JwtSignatureException extends InvalidJwtException {
    private static final long serialVersionUID = 1L;

    public JwtSignatureException(String message) {
        super(message);
    }

    public JwtSignatureException(String message, Throwable cause) {
        super(message, cause);
    }
}
