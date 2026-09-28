package com.phuoc.controllers;

import com.phuoc.exception.InvalidJwtException;
import com.phuoc.exception.JwtExpiredException;
import com.phuoc.exception.JwtSignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Buoc 10: Nem Exception - bang loi xac thuc trong slide:
 *  BadCredentials 401 | AccountStatus 403 | AccessDenied 403 | chu ky sai 401 | JWT het han 401
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException exception) {
        return problem(HttpStatus.UNAUTHORIZED, exception, "The username or password is incorrect");
    }

    @ExceptionHandler(AccountStatusException.class)
    public ProblemDetail handleAccountStatus(AccountStatusException exception) {
        return problem(HttpStatus.FORBIDDEN, exception, "The account is locked");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException exception) {
        return problem(HttpStatus.FORBIDDEN, exception, "You are not authorized to access this resource");
    }

    @ExceptionHandler(JwtSignatureException.class)
    public ProblemDetail handleJwtSignature(JwtSignatureException exception) {
        return problem(HttpStatus.UNAUTHORIZED, exception, "The JWT signature is invalid");
    }

    @ExceptionHandler(JwtExpiredException.class)
    public ProblemDetail handleJwtExpired(JwtExpiredException exception) {
        return problem(HttpStatus.UNAUTHORIZED, exception, "The JWT token has expired");
    }

    @ExceptionHandler(InvalidJwtException.class)
    public ProblemDetail handleInvalidJwt(InvalidJwtException exception) {
        return problem(HttpStatus.UNAUTHORIZED, exception, "The JWT is invalid");
    }

    /** Vi du: token hop le nhung user da bi xoa (UsernameNotFoundException). */
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException exception) {
        return problem(HttpStatus.UNAUTHORIZED, exception, "Authentication failed");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadRequest(IllegalArgumentException exception) {
        return problem(HttpStatus.BAD_REQUEST, exception, exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleSecurityException(Exception exception) {
        // TODO send this stack trace to an observability tool
        exception.printStackTrace();
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, exception, "Unknown internal server error.");
    }

    private ProblemDetail problem(HttpStatus status, Exception exception, String description) {
        ProblemDetail errorDetail = ProblemDetail.forStatusAndDetail(status, exception.getMessage());
        errorDetail.setProperty("description", description);
        return errorDetail;
    }
}
