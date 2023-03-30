package com.annex.backend.security;

import com.annex.backend.config.RsaKeyProperties;
import com.annex.backend.config.UserDetailsService;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@AllArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final RsaKeyProperties rsaKeyProperties;
    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestJwt = getJwtFromRequest(request);
        if(requestJwt != null && !requestJwt.isBlank() && validateToken(requestJwt)){

            String mail = jwtProvider.getEmailFromJwt(requestJwt);
            UserDetails userDetails = userDetailsService.loadUserByUsername(mail);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");
        System.out.println("auth:" + request.getHeader("Authorization"));
        if(bearerToken != null && !bearerToken.isBlank() && bearerToken.contains("Bearer") && bearerToken.startsWith("Bearer ")){
            return bearerToken = bearerToken.split(" ")[1];
        }

        return bearerToken;
    }

    public boolean validateToken(String jwt){
            Jwts.parserBuilder().setSigningKey(rsaKeyProperties.publicKey()).build().parseClaimsJws(jwt);
            return true;
    }
}
