package com.github.nikit.cpp.dto;

import java.io.Serializable;
import java.net.URL;

/**
 * Created by nik on 22.06.17.
 */
public class UserDTO implements Serializable {
    private static final long serialVersionUID = -5796134399691582320L;

    private Long id;

    private String login;

    private URL avatar;

    public UserDTO(Long id, String login) {
        this.id = id;
        this.login = login;
    }

    public UserDTO(Long id, String login, URL avatar) {
        this(id, login);
        this.avatar = avatar;
    }


    public UserDTO() { }

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

    public URL getAvatar() {
        return avatar;
    }

    public void setAvatar(URL avatar) {
        this.avatar = avatar;
    }
}
