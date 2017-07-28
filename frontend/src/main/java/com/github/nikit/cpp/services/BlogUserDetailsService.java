package com.github.nikit.cpp.services;

import com.github.nikit.cpp.converter.UserAccountConverter;
import com.github.nikit.cpp.dto.UserAccountDetailsDTO;
import com.github.nikit.cpp.repo.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class BlogUserDetailsService implements UserDetailsService {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Override
    public UserAccountDetailsDTO loadUserByUsername(String username) throws UsernameNotFoundException {
        return userAccountRepository
                .findByUsername(username)
                .map(UserAccountConverter::convertToUserAccountDetailsDTO)
                .orElseThrow(() -> new UsernameNotFoundException("User with login '" + username + "' not found"));
    }

}
