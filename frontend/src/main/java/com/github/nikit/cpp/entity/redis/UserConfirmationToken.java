package com.github.nikit.cpp.entity.redis;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash
public class UserConfirmationToken {
    @Id
    private String token;

    private Long userId;

    @TimeToLive
    private long ttlSeconds;

    public UserConfirmationToken() { }

    public UserConfirmationToken(String token, Long userId, long ttlSeconds) {
        this.token = token;
        this.userId = userId;
        this.ttlSeconds = ttlSeconds;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }
}
