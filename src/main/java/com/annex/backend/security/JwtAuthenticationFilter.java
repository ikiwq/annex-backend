package com.annex.backend.security;

import com.annex.backend.config.RsaKeyProperties;
import com.annex.backend.config.UserDetailsService;
import com.annex.backend.services.RefreshTokenService;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@AllArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final RsaKeyProperties rsaKeyProperties;
    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;

    //This class is called everytime a request is made to the server.
    //It filters the request and handles authentication logic.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //We get every cookie inside the reqeust.
        Cookie[] cookies = request.getCookies();

        String authToken = null;
        String refreshToken = null;

        //By iterating through the cookies we can get the authentication token and the refresh token
        if(cookies != null && cookies.length > 0) {
            for (Cookie temp : cookies) {
                if ("annex.auth".equals(temp.getName())) {
                    authToken = temp.getValue();
                }
                if ("annex.refresh".equals(temp.getName())) {
                    refreshToken = temp.getValue();
                }
            }
        }

        //If our auth token is null, that means the user is not logged
        if(authToken == null){
            filterChain.doFilter(request, response);
            return;
        }

        if(!validateToken(authToken)){
            //If the token is not valid, the token might be expired.
            //Take the mail from the refresh token
            String userMail = refreshTokenService.validateRefreshToken(refreshToken).getMail();

            //Generate a new refresh token, set that as http only and delete the old one.
            Cookie newRefCookie = new Cookie("annex.refresh", refreshTokenService.generateRefreshToken(userMail).getToken());
            newRefCookie.setHttpOnly(true);
            refreshTokenService.deleteRefreshToken(refreshToken);

            //Generate a new JWT auth token
            authToken = jwtProvider.generateTokenWithMail(userMail);
            Cookie newAuthCookie = new Cookie("annex.auth", authToken);
            newAuthCookie.setHttpOnly(true);

            response.addCookie(newRefCookie);
            response.addCookie(newAuthCookie);
        }

        //Get mail from the JWT
        String mail = jwtProvider.getEmailFromJwt(authToken);
        //Generate a new UserDetails class. The UserDetailsService retrieves the user from the database.
        UserDetails userDetails = userDetailsService.loadUserByUsername(mail);

        //To make the credentials available throughout the application, we need to create a new UsernamePasswordAuthenticationToken
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        //Then, we can set the SecurityContextHolder context with the token.
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        filterChain.doFilter(request, response);
    }

    public boolean validateToken(String jwt){
        try{
            Jwts.parserBuilder().setSigningKey(rsaKeyProperties.publicKey()).build().parseClaimsJws(jwt);
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
