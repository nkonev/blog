package com.github.nikit.cpp.dto;

import com.github.nikit.cpp.entity.UserAccount;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;

/**
 * Internal class for Spring Security, it shouldn't be passed to browser via Rest API
 */
public class UserAccountDetailsDTO extends UserAccountDTO implements UserDetails {
    private static final long serialVersionUID = -3271989114498135073L;

    private String password;
    private boolean expired;
    private boolean locked;
    private boolean enabled; // synonym to "confirmed"

    private Collection<SimpleGrantedAuthority> roles = new HashSet<>();

    public UserAccountDetailsDTO() { }

    public UserAccountDetailsDTO(
            Long id,
            String login,
            URL avatar,
            String password,
            boolean expired,
            boolean locked,
            boolean enabled,
            Collection<SimpleGrantedAuthority> roles
    ) {
        super(id, login, avatar);
        this.password = password;
        this.expired = expired;
        this.locked = locked;
        this.enabled = enabled;
        this.roles = roles;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return super.getLogin();
    }

    @Override
    public boolean isAccountNonExpired() {
        return !expired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Collection<SimpleGrantedAuthority> getRoles() {
        return roles;
    }

    public void setRoles(Collection<SimpleGrantedAuthority> roles) {
        this.roles = roles;
    }
}
