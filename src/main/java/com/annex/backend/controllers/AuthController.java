package com.annex.backend.controllers;

import com.annex.backend.dto.*;
import com.annex.backend.services.AuthService;
import com.annex.backend.services.RefreshTokenService;
import com.annex.backend.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest){
        return authService.register(registerRequest);
    }

    @GetMapping("/verify/{token}")
    public ResponseEntity<String> verifyAccount(@PathVariable String token){
        return authService.verifyAccount(token);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest loginRequest){
        return authService.login(loginRequest);
    }

    @PostMapping("/refresh/token")
    public ResponseEntity<AuthenticationResponse> refreshTokens(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest){
        return authService.refreshToken(refreshTokenRequest);
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logout(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest){
        refreshTokenService.deleteRefreshToken(refreshTokenRequest.getRefreshToken());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/getCurrentUser")
    public ResponseEntity<UserResponse> getUserByToken(){
        return userService.getUserByToken();
    }
}
