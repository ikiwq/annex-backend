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
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    //Ignore. That was part of an old email verification system.
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

    public ResponseEntity login(@RequestBody LoginRequest loginRequest, HttpServletResponse httpServletResponse){
        //By creating a username and password authentication token we can use BCrypt's encryption and decryption system
        UsernamePasswordAuthenticationToken userToLog =
                new UsernamePasswordAuthenticationToken(loginRequest.getUsercred(), loginRequest.getPassword());

        Authentication authentication = null;

        if(userRepository.findByUsername(loginRequest.getUsercred()).isEmpty() && userRepository.findByEmail(loginRequest.getUsercred()).isEmpty()){
            return new ResponseEntity<String>("User does not exist!", HttpStatus.BAD_REQUEST);
        }

        try{
             authentication = authenticationManager
                    .authenticate(userToLog);
        }catch (Exception e){
            return new ResponseEntity<String>("Wrong credentials", HttpStatus.BAD_REQUEST);
        }

        //If the login is successful, we can set the security context.
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String AuthToken = jwtProvider.generateToken(authentication);

        AuthenticationResponse newAuth = new AuthenticationResponse();

        //Once we are logged in, we can generate a new auth token and a new refresh token.
        Cookie authCookie = new Cookie("annex.auth", AuthToken);
        authCookie.setHttpOnly(true);
        httpServletResponse.addCookie(authCookie);

        Cookie refreshCookie = new Cookie("annex.refresh", refreshTokenService.generateRefreshToken(authentication.getName()).getToken());
        refreshCookie.setHttpOnly(true);
        httpServletResponse.addCookie(refreshCookie);

        newAuth.setExpiresAt(Instant.now().plusMillis(900000));
        newAuth.setMail(authentication.getName());

        return new ResponseEntity<AuthenticationResponse>(newAuth, HttpStatus.OK);
    }

    public ResponseEntity logout(HttpServletRequest request, HttpServletResponse response){
        Cookie[] cookies = request.getCookies();

        Cookie refreshCookie = null;
        Cookie authCookie = null;

        //When logging out, we need to invalidate the tokens.
        //First, we get the authentication and the refresh tokens.
        if(cookies != null && cookies.length > 0){
            for (Cookie temp : cookies){
                if("annex.refresh".equals(temp.getName())){
                    refreshCookie = temp;
                }
                if("annex.auth".equals(temp.getName())){
                    authCookie = temp;
                }
            }
        }

        //And then we set the max age to zero and invalidate its content
        if(refreshCookie != null){
            //Also, delete the refresh token.
            refreshTokenService.deleteRefreshToken(refreshCookie.getValue());
            refreshCookie.setMaxAge(0);
            refreshCookie.setValue("");
            response.addCookie(refreshCookie);
        }
        if(authCookie != null){
            authCookie.setMaxAge(0);
            authCookie.setValue("");
            response.addCookie(authCookie);
        }

        return new ResponseEntity("Logged out!", HttpStatus.OK);
    }
}
