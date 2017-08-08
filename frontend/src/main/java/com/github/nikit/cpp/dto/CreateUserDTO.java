package com.github.nikit.cpp.dto;

import com.github.nikit.cpp.Constants;
import org.hibernate.validator.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.net.URL;

public class CreateUserDTO extends UserAccountDTO {
    private static final long serialVersionUID = 4886815878934395026L;

    @Size(min = Constants.MIN_PASSWORD_LENGTH, max = Constants.MAX_PASSWORD_LENGTH)
    @NotEmpty
    private String password; // password which user desires

    @NotEmpty
    private String email;

    public CreateUserDTO() { }

    public CreateUserDTO(Long id, String login, URL avatar, String password, String email) {
        super(id, login, avatar);
        this.password = password;
        this.email = email;
    }

    public CreateUserDTO(String login, URL avatar, String password, String email) {
        this(null, login, avatar, password, email);
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
