package com.github.nikit.cpp.dto;

import org.hibernate.validator.constraints.NotEmpty;
import java.net.URL;

public class EditUserDTO {
    @NotEmpty
    private String login;

    private URL avatar;

    private String password; // password which user desires

    @NotEmpty
    private String email;

    public EditUserDTO() { }

    public EditUserDTO(String login, URL avatar, String password, String email) {
        this.login = login;
        this.avatar = avatar;
        this.password = password;
        this.email = email;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public URL getAvatar() {
        return avatar;
    }

    public void setAvatar(URL avatar) {
        this.avatar = avatar;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
