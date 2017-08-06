package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.dto.CreateUserDTO;
import com.github.nikit.cpp.entity.jpa.UserAccount;
import com.github.nikit.cpp.entity.jpa.UserRole;
import com.github.nikit.cpp.entity.redis.UserConfirmationToken;
import com.github.nikit.cpp.exception.UserAlreadyPresentException;
import com.github.nikit.cpp.repo.jpa.UserAccountRepository;
import com.github.nikit.cpp.repo.redis.UserConfirmationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Controller
@Transactional
public class RegistrationController {
    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private UserConfirmationTokenRepository userConfirmationTokenRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${custom.registration.email.from}")
    private String from;

    @Value("${custom.registration.email.subject}")
    private String subject;

    @Value("${custom.registration.email.text-template}")
    private String textTemplate;

    @Value("${custom.base-url}")
    private String baseUrl;

    @Value("${custom.confirmation.registration.token.ttl-minutes}")
    private long userConfirmationTokenTtlMinutes;

    private static final String REG_LINK_PLACEHOLDER = "__REGISTRATION_LINK_PLACEHOLDER__";


    @PostMapping(value = Constants.Uls.API+"/register")
    @ResponseBody
    public void register(@RequestBody CreateUserDTO userAccountDTO) {
        if(userAccountRepository.findByUsername(userAccountDTO.getLogin()).isPresent()){
            throw new UserAlreadyPresentException("User with login '" + userAccountDTO.getLogin() + "' is already present");
        }
        if(userAccountRepository.findByEmail(userAccountDTO.getEmail()).isPresent()){
            return; // we care for user email leak
        }

        Set<UserRole> newUserRoles = new HashSet<>();
        newUserRoles.add(UserRole.ROLE_USER);

        boolean expired = false;
        boolean locked = false;
        boolean enabled = false;

        UserAccount userAccount = new UserAccount(
                userAccountDTO.getLogin(),
                userAccountDTO.getPassword(),
                userAccountDTO.getAvatar(),
                expired,
                locked,
                enabled,
                newUserRoles,
                userAccountDTO.getEmail()
        );
        userAccount = userAccountRepository.save(userAccount);

        Duration ttl = Duration.ofMinutes(userConfirmationTokenTtlMinutes);
        long seconds = ttl.getSeconds(); // Redis requires seconds

        UUID tokenUuid = UUID.randomUUID();
        UserConfirmationToken userConfirmationToken = new UserConfirmationToken(tokenUuid.toString(), userAccount.getId(), seconds);
        userConfirmationToken = userConfirmationTokenRepository.save(userConfirmationToken);

        // https://yandex.ru/support/mail-new/mail-clients.html
        // https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-email.html
        // http://docs.spring.io/spring/docs/4.3.10.RELEASE/spring-framework-reference/htmlsingle/#mail-usage-simple
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setSubject(subject);
        msg.setTo(userAccount.getEmail());

        String text = textTemplate.replace(REG_LINK_PLACEHOLDER, baseUrl + "/confirm?uuid=" + tokenUuid);
        msg.setText(text);

        mailSender.send(msg);
    }

    @PostMapping(value = Constants.Uls.API+"/reset-password")
    @ResponseBody
    public Map<String, String> resetPassword() {
        return null;
    }

    /**
     * Handles confirmations.
     * In frontend router also should implement follows pages
     * /confirm -- success confirmation
     * /confirm/registration/token-not-found
     * /confirm/registration/user-not-found
     * @param uuid
     * @return
     */
    @GetMapping(value = "/confirm")
    public String confirm(@RequestParam("uuid") UUID uuid) {
        String stringUuid = uuid.toString();
        UserConfirmationToken userConfirmationToken = userConfirmationTokenRepository.findOne(stringUuid);
        if (userConfirmationToken == null) {
            return "redirect:/confirm/registration/token-not-found";
        }

        UserAccount userAccount = userAccountRepository.findOne(userConfirmationToken.getUserId());
        if (userAccount == null) {
            return "redirect:/confirm/registration/user-not-found";
        }

        userAccount.setEnabled(true);

        userConfirmationTokenRepository.delete(stringUuid);

        return Constants.Uls.ROOT;
    }

    @GetMapping(value = Constants.Uls.API+"/resend-email")
    @ResponseBody
    public Map<String, String> resend() {
        // send new
        return null;
    }

}
