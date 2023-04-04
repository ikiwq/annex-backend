package com.annex.backend.security;

import com.annex.backend.config.RsaKeyProperties;
import com.annex.backend.config.UserDetailsService;
import com.annex.backend.models.RefreshToken;
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

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Cookie[] cookies = request.getCookies();

        String authToken = null;
        String refreshToken = null;

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

        if(authToken == null){
            filterChain.doFilter(request, response);
            return;
        }

        if(!validateToken(authToken)){
            String userMail = refreshTokenService.validateRefreshToken(refreshToken).getMail();

            Cookie newRefCookie = new Cookie("annex.refresh", refreshTokenService.generateRefreshToken(userMail).getToken());
            newRefCookie.setHttpOnly(true);
            refreshTokenService.deleteRefreshToken(refreshToken);

            authToken = jwtProvider.generateTokenWithMail(userMail);
            Cookie newAuthCookie = new Cookie("annex.auth", authToken);
            newAuthCookie.setHttpOnly(true);

            response.addCookie(newRefCookie);
            response.addCookie(newAuthCookie);
        }

        String mail = jwtProvider.getEmailFromJwt(authToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(mail);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
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
