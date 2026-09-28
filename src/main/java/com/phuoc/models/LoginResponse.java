package com.phuoc.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Buoc 3: Models */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginResponse {
    private String token;

    private long expiresIn;
}
