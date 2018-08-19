package com.github.nkonev.blog.security;

import com.github.nkonev.blog.security.checks.BlogPostAuthenticationChecks;
import com.github.nkonev.blog.security.checks.BlogPreAuthenticationChecks;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import java.util.Map;

public class CheckedUserInfoTokenServices extends UserInfoTokenServices implements MessageSourceAware {
    private PrincipalExtractor principalExtractor;
    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
    private UserDetailsChecker preAuthenticationChecks;
    private UserDetailsChecker postAuthenticationChecks;

    public CheckedUserInfoTokenServices(String userInfoEndpointUrl, String clientId, PrincipalExtractor principalExtractor,
                                        BlogPreAuthenticationChecks pre, BlogPostAuthenticationChecks post) {
        super(userInfoEndpointUrl, clientId);
        this.principalExtractor = principalExtractor;
        this.preAuthenticationChecks = pre;
        this.postAuthenticationChecks = post;
    }

    @Override
    protected Object getPrincipal(Map<String, Object> map) {
        Object principal = this.principalExtractor.extractPrincipal(map);
        if (principal instanceof UserDetails){
            checkUserDetails((UserDetails)principal);
        } else {
            throw new RuntimeException("principal should be instance of "+UserDetails.class);
        }
        return principal;
    }

    private void checkUserDetails(UserDetails principal) {
        preAuthenticationChecks.check(principal);
        postAuthenticationChecks.check(principal);
    }

    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messages = new MessageSourceAccessor(messageSource);
    }

    @Override
    public void setPrincipalExtractor(PrincipalExtractor principalExtractor){
        throw new UnsupportedOperationException("not supported. use constructor.");
    }
}
