package com.annex.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    private String authToken;
    private String refreshToken;
    private String mail;
    private Instant expiresAt;
}
