package com.github.nkonev.entity.jpa;

import com.github.nkonev.Constants;
import com.vladmihalcea.hibernate.type.array.StringArrayType;
import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;

@TypeDefs({
        @TypeDef(
                name = "pgsql_enum",
                typeClass = PostgreSQLEnumType.class
        )
})
@Entity
@Table(name = "users", schema = Constants.Schemas.AUTH)
public class UserAccount {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private String username;
    private String password; // hash
    private String avatar;
    private boolean expired;
    private boolean locked;
    private boolean enabled; // synonym to "confirmed"
    @Email
    private String email;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Type(type = "pgsql_enum")
    @Column(
            name = "role",
            columnDefinition = "user_role"
    )
    private UserRole role; // synonym to "authority"

    public UserAccount() { }

    public UserAccount(String username, String password, String avatar, boolean expired, boolean locked, boolean enabled, UserRole role, String email) {
        this.username = username;
        this.password = password;
        this.avatar = avatar;
        this.expired = expired;
        this.locked = locked;
        this.enabled = enabled;
        this.role = role;
        this.email = email;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
