package com.github.nkonev.entity.jpa;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(schema = "historical", name = "password_reset_token")
public class PasswordResetToken {
    @Id
    private UUID uuid;

    private Long userId;

    private LocalDateTime expiredAt;

    public PasswordResetToken() { }

    public PasswordResetToken(UUID uuid, Long userId, LocalDateTime expiredAt) {
        this.uuid = uuid;
        this.userId = userId;
        this.expiredAt = expiredAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(LocalDateTime expiredAt) {
        this.expiredAt = expiredAt;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
