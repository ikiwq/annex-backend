package com.annex.backend.services;

import com.annex.backend.models.RefreshToken;
import com.annex.backend.repositories.RefreshTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public RefreshToken generateRefreshToken(){
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setCreatedAt(Instant.now());

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    void validateRefreshToken(String token){
        refreshTokenRepository.findByToken(token)
                .orElseThrow(()-> new IllegalStateException("Invalid refresh Token"));
    }

    @Transactional
    public void deleteRefreshToken(String token){
        refreshTokenRepository.deleteByToken(token);
    }
}
