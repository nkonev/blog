package com.github.nikit.cpp.services;

import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.entity.jpa.PasswordResetToken;
import com.github.nikit.cpp.entity.redis.UserConfirmationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Performs sending entity to email
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${custom.base-url}")
    private String baseUrl;

    @Value("${custom.email.from}")
    private String from;

    @Value("${custom.registration.email.subject}")
    private String registrationSubject;

    @Value("${custom.registration.email.text-template}")
    private String registrationTextTemplate;


    @Value("${custom.password-reset.email.subject}")
    private String passwordResetSubject;

    @Value("${custom.password-reset.email.text-template}")
    private String passwordResetTextTemplate;

    private static final String REG_LINK_PLACEHOLDER = "__REGISTRATION_LINK_PLACEHOLDER__";
    private static final String PASSWORD_RESET_LINK_PLACEHOLDER = "__PASSWORD_RESET_LINK_PLACEHOLDER__";

    public void sendUserConfirmationToken(String email, UserConfirmationToken userConfirmationToken) {
        // https://yandex.ru/support/mail-new/mail-clients.html
        // https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-email.html
        // http://docs.spring.io/spring/docs/4.3.10.RELEASE/spring-framework-reference/htmlsingle/#mail-usage-simple
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setSubject(registrationSubject);
        msg.setTo(email);

        String text = registrationTextTemplate.replace(REG_LINK_PLACEHOLDER, baseUrl + Constants.Uls.CONFIRM+ "?"+Constants.Uls.UUID +"=" + userConfirmationToken.getUuid());
        msg.setText(text);

        mailSender.send(msg);
    }

    public void sendPasswordResetToken(String email, PasswordResetToken passwordResetToken) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setSubject(passwordResetSubject);
        msg.setTo(email);

        String text = passwordResetTextTemplate.replace(PASSWORD_RESET_LINK_PLACEHOLDER, baseUrl + Constants.Uls.PASSWORD_RESET+ "?"+Constants.Uls.UUID +"=" + passwordResetToken.getUuid());
        msg.setText(text);

        mailSender.send(msg);
    }

}
