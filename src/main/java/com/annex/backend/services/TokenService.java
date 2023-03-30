package com.annex.backend.services;

import com.annex.backend.models.User;
import com.annex.backend.models.VerificationToken;
import com.annex.backend.repositories.VerificationTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TokenService {

    private final VerificationTokenRepository verificationTokenRepository;

    @Transactional
    public String generateVerificationToken(User user){
        String newToken = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();

        verificationToken.setToken(newToken);
        verificationToken.setUser(user);
        verificationToken.setExpiresAt(LocalDateTime.now().plus(7, ChronoUnit.DAYS));

        verificationTokenRepository.save(verificationToken);

        return newToken;
    }

}
