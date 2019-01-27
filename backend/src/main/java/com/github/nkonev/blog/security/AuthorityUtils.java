package com.github.nkonev.blog.security;

import com.github.nkonev.blog.dto.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class AuthorityUtils {
    public static UserRole getDefaultUserRole(){
        return UserRole.ROLE_USER;
    }

    public static GrantedAuthority getDefaultGrantedAuthority(){
        return new SimpleGrantedAuthority(getDefaultUserRole().name());
    }
}
