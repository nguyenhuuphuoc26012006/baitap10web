package com.phuoc.exception;

/** JWT khong hop le noi chung (sai dinh dang, sai thuat toan...). Lop cha cua 2 loi ben duoi. */
public class InvalidJwtException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidJwtException(String message) {
        super(message);
    }

    public InvalidJwtException(String message, Throwable cause) {
        super(message, cause);
    }
}
