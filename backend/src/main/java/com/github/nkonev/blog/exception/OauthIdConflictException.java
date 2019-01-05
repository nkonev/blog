package com.github.nkonev.blog.exception;

import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;

public class OauthIdConflictException extends InvalidTokenException {
    public OauthIdConflictException(String msg) {
        super(msg);
    }
}
