package com.annex.backend.services;

import com.annex.backend.dto.*;
import com.annex.backend.models.User;
import com.annex.backend.models.VerificationToken;
import com.annex.backend.repositories.ImageRepository;
import com.annex.backend.repositories.UserRepository;
import com.annex.backend.repositories.VerificationTokenRepository;
import com.annex.backend.security.JwtProvider;
import com.annex.backend.services.mail.CredentialChecker;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.Instant;

@Service
@AllArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final ImageRepository imageRepository;

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    private final UserService userService;
    private final CredentialChecker credentialChecker;

    @Transactional
    public ResponseEntity<String> register(RegisterRequest registerRequest){

        if(registerRequest.getEmail().length() > 64 && !credentialChecker.isEmailValid(registerRequest.getEmail())){
            return new ResponseEntity<>("Please insert a valid email!", HttpStatus.BAD_REQUEST);
        }

        if(!credentialChecker.isUsernameValid(registerRequest.getUsername())){
            return new ResponseEntity<>("Please insert a valid username!", HttpStatus.BAD_REQUEST);
        }

        if(userService.doesUserExistByMail(registerRequest.getEmail())){
            return new ResponseEntity<>("User with that email already exists!", HttpStatus.BAD_REQUEST);
        }

        if(userService.doesUserExistByUsername(registerRequest.getUsername())){
            return new ResponseEntity<>("Username already taken!", HttpStatus.BAD_REQUEST);
        }

        if(registerRequest.getUsername().length() < 6 ) return new ResponseEntity<>("Username should be at least 6 characters.", HttpStatus.BAD_REQUEST);
        if(registerRequest.getUsername().length() > 20 ) return new ResponseEntity<>("Username can't be longer than 20 characters", HttpStatus.BAD_REQUEST);

        User newUser = new User();

        newUser.setUsername(registerRequest.getUsername());
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        newUser.setProfilePicture(imageRepository.findByPath("4218a483-7dfe-4bd3-9c2a-21bec7d9a32c").orElseThrow());

        newUser.setBiography("");

        newUser.setCreatedAt(Instant.now());

        newUser.setLocked(false);
        newUser.setEnabled(true);
        newUser.setAdmin(false);

        userRepository.save(newUser);

        return new ResponseEntity<>("User Registration Successful!", HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<String> verifyAccount(String string){
        VerificationToken verificationToken = verificationTokenRepository.findByToken(string).orElseThrow(()-> new IllegalStateException("Token not found"));
        fetchUserAndEnable(verificationToken);
        return new ResponseEntity<>("Account activated successfully", HttpStatus.OK);
    }

    @Transactional(readOnly = true)
    public void fetchUserAndEnable(VerificationToken verificationToken){
        String email = verificationToken.getUser().getEmail();

        User user = userRepository.findByEmail(email).orElseThrow(()->new IllegalStateException("User not found with email" + email));

        user.setEnabled(true);
        userRepository.save(user);
    }

    public ResponseEntity login(@RequestBody LoginRequest loginRequest){

        UsernamePasswordAuthenticationToken userToLog =
                new UsernamePasswordAuthenticationToken(loginRequest.getUsercred(), loginRequest.getPassword());

        Authentication authentication = null;

        if(!userRepository.findByUsername(loginRequest.getUsercred()).isPresent() && !userRepository.findByEmail(loginRequest.getUsercred()).isPresent()){
            return new ResponseEntity<String>("User does not exist!", HttpStatus.BAD_REQUEST);
        }

        try{
             authentication = authenticationManager
                    .authenticate(userToLog);
        }catch (Exception e){
            return new ResponseEntity<String>("Wrong credentials", HttpStatus.BAD_REQUEST);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String AuthToken = jwtProvider.generateToken(authentication);

        AuthenticationResponse newAuth = new AuthenticationResponse();

        newAuth.setAuthToken(AuthToken);
        newAuth.setRefreshToken(refreshTokenService.generateRefreshToken().getToken());
        newAuth.setExpiresAt(Instant.now().plusMillis(900000));
        newAuth.setMail(authentication.getName());

        return new ResponseEntity<AuthenticationResponse>(newAuth, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<AuthenticationResponse> refreshToken(RefreshTokenRequest refreshTokenRequest){
        refreshTokenService.validateRefreshToken(refreshTokenRequest.getRefreshToken());
        String authToken = jwtProvider.generateTokenWithMail(refreshTokenRequest.getMail());
        AuthenticationResponse newAuth = new AuthenticationResponse();

        newAuth.setAuthToken(authToken);
        newAuth.setRefreshToken(refreshTokenService.generateRefreshToken().getToken());
        newAuth.setMail(refreshTokenRequest.getMail());
        newAuth.setExpiresAt(Instant.now().plusMillis(900000));

        return new ResponseEntity<AuthenticationResponse>(newAuth, HttpStatus.OK);
    }
}
