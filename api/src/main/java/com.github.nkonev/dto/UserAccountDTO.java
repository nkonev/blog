package com.github.nkonev.dto;

import org.hibernate.validator.constraints.NotEmpty;
import java.io.Serializable;
import java.net.URL;

/**
 * Created by nik on 22.06.17.
 */
public class UserAccountDTO implements Serializable {
    private static final long serialVersionUID = -5796134399691582320L;

    private Long id;

    @NotEmpty
    private String login;

    private String avatar;

    public UserAccountDTO(Long id, String login) {
        this.id = id;
        this.login = login;
    }

    public UserAccountDTO(Long id, String login, String avatar) {
        this(id, login);
        this.avatar = avatar;
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
}
