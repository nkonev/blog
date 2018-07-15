package com.github.nkonev.blog.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * Created by nik on 09.07.17.
 */
@Component
public class RESTAuthenticationLogoutSuccessHandler implements LogoutSuccessHandler {

    private final CsrfTokenRepository csrfTokenRepository;

    @Autowired
    private ObjectMapper objectMapper;

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

        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        objectMapper.writeValue(response.getWriter(), Collections.singletonMap("message", "you successfully logged out"));
    }
}
