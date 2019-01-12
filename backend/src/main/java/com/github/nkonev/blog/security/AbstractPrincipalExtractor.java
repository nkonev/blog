package com.github.nkonev.blog.security;

import com.github.nkonev.blog.dto.UserAccountDetailsDTO;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class AbstractPrincipalExtractor {
    protected boolean isAlreadyAuthenticated(){
        return SecurityContextHolder.getContext().getAuthentication()!=null && SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof UserAccountDetailsDTO;
    }

    protected UserAccountDetailsDTO getPrincipal() {
        return (UserAccountDetailsDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
