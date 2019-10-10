package com.github.nkonev.blog.dto;


import java.time.LocalDateTime;
import java.util.Collection;

/**
 * Class which displays in user's profile page. It will be POSTed as EditUserDTO
 */
public class UserSelfProfileDTO extends UserAccountDTO {

    private static final long serialVersionUID = -375973022870811159L;

    private String email;

    private Collection<UserRole> roles;

    public UserSelfProfileDTO() { }

    public UserSelfProfileDTO(Long id, String login, String avatar, String email, LocalDateTime lastLoginDateTime, OauthIdentifiersDTO oauthIdentifiers, Collection<UserRole> roles) {
        super(id, login, avatar, lastLoginDateTime, oauthIdentifiers);
        this.email = email;
        this.roles = roles;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Collection<UserRole> getRoles() {
        return roles;
    }

    public void setRoles(Collection<UserRole> roles) {
        this.roles = roles;
    }
}
