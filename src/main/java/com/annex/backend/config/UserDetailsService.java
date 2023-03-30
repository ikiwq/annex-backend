package com.annex.backend.config;

import com.annex.backend.models.User;
import com.annex.backend.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String loginName) throws UsernameNotFoundException {
        Optional<User> user= userRepository.findByUsername(loginName);
        if(!user.isPresent()){
            user = userRepository.findByEmail(loginName);
            if(!user.isPresent()) throw new IllegalStateException("Can't find user!");
        }

        User currentUser = user.get();

        return new org.springframework.security.core.userdetails.User(currentUser.getEmail(),
                currentUser.getPassword(), currentUser.isEnabled(),
                true, true, !currentUser.isLocked(), getAuthorities("USER"));
    }

    private Collection<? extends GrantedAuthority> getAuthorities(String role){
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }
}
