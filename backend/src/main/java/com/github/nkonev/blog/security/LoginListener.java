package com.github.nkonev.blog.security;

import com.github.nkonev.blog.entity.jpa.UserAccount;
import com.github.nkonev.blog.repo.jpa.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static com.github.nkonev.blog.utils.TimeUtil.getNowUTC;

/**
 * This listener works for both database and OAuth2 logins
 */
@Transactional
@Component
public class LoginListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginListener.class);

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        UserDetails userDetails = (UserDetails) event.getAuthentication().getPrincipal();
        LOGGER.info("User '{}' logged in", userDetails.getUsername());
        UserAccount userAccount = userAccountRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        userAccount.setLastLoginDateTime(getNowUTC());
    }
}