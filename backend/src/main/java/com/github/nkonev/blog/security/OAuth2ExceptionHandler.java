package com.github.nkonev.blog.security;

import com.github.nkonev.blog.exception.OauthIdConflictException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class OAuth2ExceptionHandler extends SimpleUrlAuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {

        if (exception.getCause() instanceof OauthIdConflictException){
            OauthIdConflictException oauthIdConflictException = (OauthIdConflictException) exception.getCause();
            response.getOutputStream().println(oauthIdConflictException.getMessage());
            response.setStatus(403);
        } else {
            super.onAuthenticationFailure(request, response, exception);
        }
    }

}
