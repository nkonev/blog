package com.github.nkonev.blog.exception;


public class OauthIdConflictException extends RuntimeException {
    public OauthIdConflictException(String msg) {
        super(msg);
    }
}
