package com.github.nikit.cpp.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.util.Assert;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by nik on 09.07.17.
 */

public class RESTAuthenticationLogoutSuccessHandler implements LogoutSuccessHandler {

    private final CsrfTokenRepository csrfTokenRepository;

    public RESTAuthenticationLogoutSuccessHandler(CsrfTokenRepository csrfTokenRepository) {
        Assert.notNull(csrfTokenRepository, "csrfTokenRepository cannot be null");
        this.csrfTokenRepository = csrfTokenRepository;
    }


    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // do nothing -- it's enough to return 200 for SPA

        // set new csrf token for repeating logins without page reload
        CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
        this.csrfTokenRepository.saveToken(csrfToken, request, response);
    }
}
