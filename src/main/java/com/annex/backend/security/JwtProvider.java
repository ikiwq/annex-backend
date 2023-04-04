package com.annex.backend.security;

import com.annex.backend.config.RsaKeyProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.Instant;

@Service
@Data
@AllArgsConstructor
public class JwtProvider {

    private final RsaKeyProperties rsaKeyProperties;

    private static final int jwtExpirationMillis = 30000;

    public String generateToken(Authentication authentication){
        return Jwts
                .builder().setSubject(authentication.getName())
                .signWith(rsaKeyProperties.privateKey())
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusMillis(jwtExpirationMillis)))
                .compact();
    }

    public String generateTokenWithMail(String mail){
        return Jwts
                .builder().setSubject(mail).signWith(rsaKeyProperties.privateKey())
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusMillis(jwtExpirationMillis)))
                .compact();
    }

    public String getEmailFromJwt(String token){
        Claims claims = Jwts.parserBuilder().setSigningKey(rsaKeyProperties.publicKey()).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }
}
