package com.github.nkonev.blog.security;

import org.springframework.util.Assert;

import static com.github.nkonev.blog.security.FacebookPrincipalExtractor.LOGIN_PREFIX;

public class LoginUtils {
    public static String validateAndTrimLogin(String login){
        Assert.notNull(login, "login cannot be null");
        login = login.trim();
        Assert.hasLength(login, "login should have length");
        Assert.isTrue(!login.startsWith(LOGIN_PREFIX));
        return login;
    }
}
