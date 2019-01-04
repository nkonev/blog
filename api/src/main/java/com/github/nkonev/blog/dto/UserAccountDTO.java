package com.github.nkonev.blog.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.nkonev.blog.ApiConstants;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Created by nik on 22.06.17.
 * Contains public information
 */
public class UserAccountDTO implements Serializable {
    private static final long serialVersionUID = -5796134399691582320L;

    private Long id;

    @NotEmpty
    private String login;

    private String avatar;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern= ApiConstants.DATE_FORMAT)
    private LocalDateTime lastLoginDateTime;

    private String facebookId;

    private String vkontakteId;

    public UserAccountDTO(Long id, String login, String avatar, LocalDateTime lastLoginDateTime, String facebookId, String vkontakteId) {
        this.id = id;
        this.login = login;
        this.avatar = avatar;
        this.lastLoginDateTime = lastLoginDateTime;
        this.facebookId = facebookId;
        this.vkontakteId = vkontakteId;
    }


    public UserAccountDTO() { }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public String getVkontakteId() {
        return vkontakteId;
    }

    public void setVkontakteId(String vkontakteId) {
        this.vkontakteId = vkontakteId;
    }

    public LocalDateTime getLastLoginDateTime() {
        return lastLoginDateTime;
    }

    public void setLastLoginDateTime(LocalDateTime lastLoginDateTime) {
        this.lastLoginDateTime = lastLoginDateTime;
    }
}
