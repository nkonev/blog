package com.github.nikit.cpp.entity;

import javax.persistence.*;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;

@Entity
@Table(name = "users", schema = "auth")
public class UserAccount {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private String username;
    private String password; // hash
    private URL avatar;
    private boolean expired;
    private boolean locked;
    private boolean enabled; // synonym to "confirmed"

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="roles", schema = "auth", joinColumns=@JoinColumn(name="user_id"))
    @Column(name="role_name")
    private Collection<String> roles = new HashSet<>(); // synonym to "authority"

    public UserAccount() { }

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

    public Collection<String> getRoles() {
        return roles;
    }

    public void setRoles(Collection<String> roles) {
        this.roles = roles;
    }
}
