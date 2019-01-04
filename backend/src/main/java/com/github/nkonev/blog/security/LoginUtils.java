package com.github.nkonev.blog.security;

import org.springframework.util.Assert;

public class LoginUtils {
    public static String validateAndTrimLogin(String login){
        Assert.notNull(login, "login cannot be null");
        login = login.trim();
        Assert.hasLength(login, "login should have length");
        Assert.isTrue(!login.startsWith(FacebookPrincipalExtractor.LOGIN_PREFIX), "not allowed prefix");
        Assert.isTrue(!login.startsWith(VkontaktePrincipalExtractor.LOGIN_PREFIX), "not allowed prefix");

        return login;
    }
}
