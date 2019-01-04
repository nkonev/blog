package com.github.nkonev.blog.dto;


import java.time.LocalDateTime;

/**
 * Class which displays in user's profile page. It will be POSTed as EditUserDTO
 */
public class UserSelfProfileDTO extends UserAccountDTO {

    private static final long serialVersionUID = -375973022870811159L;

    private String email;

    public UserSelfProfileDTO() { }

    public UserSelfProfileDTO(Long id, String login, String avatar, String email, LocalDateTime lastLoginDateTime, String facebookId, String vkontakteId) {
        super(id, login, avatar, lastLoginDateTime, facebookId, vkontakteId);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
