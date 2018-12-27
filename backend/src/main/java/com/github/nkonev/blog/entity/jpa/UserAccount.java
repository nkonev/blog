package com.github.nkonev.blog.entity.jpa;

import com.github.nkonev.blog.Constants;
import com.vladmihalcea.hibernate.type.array.EnumArrayType;
import com.vladmihalcea.hibernate.type.array.internal.EnumArrayTypeDescriptor;
import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Collection;

@TypeDefs({
        @TypeDef(
                name = "pgsql_enum",
                typeClass = PostgreSQLEnumType.class
        ),
        @TypeDef(
                name = "pgsql_array",
                typeClass = EnumArrayType.class
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
    private String email;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Type(type = "pgsql_enum")
    @Column(
            name = "creation_type",
            columnDefinition = "user_creation_type"
    )
    private CreationType creationType;
    private String facebookId;

    @NotNull
    //@Enumerated(EnumType.STRING)
    @Type(type = "pgsql_array", parameters = {@org.hibernate.annotations.Parameter(name = "sql_array_type", value = "auth.user_role")})
    @Column(
            name = "roles",
            columnDefinition = "\"auth\".\"_user_role\"" // first _ means array
    )
    private UserRole roles[]; // synonym to "authority"

    public UserAccount() { }

    public UserAccount(CreationType creationType, String username, String password, String avatar, boolean expired, boolean locked, boolean enabled, Collection<UserRole> roles, String email, String facebookId) {
        this.creationType = creationType;
        this.username = username;
        this.password = password;
        this.avatar = avatar;
        this.expired = expired;
        this.locked = locked;
        this.enabled = enabled;
        this.roles = roles.toArray(UserRole[]::new);
        this.email = email;
        this.facebookId = facebookId;
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

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public CreationType getCreationType() {
        return creationType;
    }

    public void setCreationType(CreationType creationType) {
        this.creationType = creationType;
    }

    public UserRole[] getRoles() {
        return roles;
    }

    public void setRoles(UserRole[] roles) {
        this.roles = roles;
    }
}
