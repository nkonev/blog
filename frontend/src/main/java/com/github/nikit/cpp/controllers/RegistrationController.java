package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.dto.CreateUserDTO;
import com.github.nikit.cpp.entity.jpa.UserAccount;
import com.github.nikit.cpp.entity.jpa.UserRole;
import com.github.nikit.cpp.entity.jpa.PasswordResetToken;
import com.github.nikit.cpp.entity.redis.UserConfirmationToken;
import com.github.nikit.cpp.exception.PasswordResetTokenNotFoundException;
import com.github.nikit.cpp.exception.UserAlreadyPresentException;
import com.github.nikit.cpp.repo.jpa.UserAccountRepository;
import com.github.nikit.cpp.repo.jpa.PasswordResetTokenRepository;
import com.github.nikit.cpp.repo.redis.UserConfirmationTokenRepository;
import com.github.nikit.cpp.utils.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@Transactional
public class RegistrationController {
    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private UserConfirmationTokenRepository userConfirmationTokenRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

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


    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationController.class);

    private UserConfirmationToken createUserConfirmationToken(UserAccount userAccount) {
        Assert.isTrue(!userAccount.isEnabled(), "user account mustn't be enabled");

        Duration ttl = Duration.ofMinutes(userConfirmationTokenTtlMinutes);
        long seconds = ttl.getSeconds(); // Redis requires seconds

        UUID tokenUuid = UUID.randomUUID();
        UserConfirmationToken userConfirmationToken = new UserConfirmationToken(tokenUuid.toString(), userAccount.getId(), seconds);
        return userConfirmationTokenRepository.save(userConfirmationToken);
    }

    private void sendUserConfirmationToken(String email, UserConfirmationToken userConfirmationToken) {
        // https://yandex.ru/support/mail-new/mail-clients.html
        // https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-email.html
        // http://docs.spring.io/spring/docs/4.3.10.RELEASE/spring-framework-reference/htmlsingle/#mail-usage-simple
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setSubject(subject);
        msg.setTo(email);

        String text = textTemplate.replace(REG_LINK_PLACEHOLDER, baseUrl + Constants.Uls.CONFIRM+ "?"+Constants.Uls.UUID +"=" + userConfirmationToken.getUuid());
        msg.setText(text);

        mailSender.send(msg);
    }

    private void sendPasswordResetToken(String email, PasswordResetToken passwordResetToken) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setSubject("password reset"); // todo
        msg.setTo(email);

        // TODO
        String LINK_PLACEHOLDER = "__LINK__";
        String textTemplate2 = "Link "+LINK_PLACEHOLDER+" for reset your password. If you didn't issue password reset please contact us";

        String text = textTemplate2.replace(LINK_PLACEHOLDER, baseUrl + "/password-reset"+ "?"+Constants.Uls.UUID +"=" + passwordResetToken.getUuid());
        msg.setText(text);

        mailSender.send(msg);
    }


    @PostMapping(value = Constants.Uls.API+Constants.Uls.REGISTER)
    @ResponseBody
    public void register(@RequestBody @Valid CreateUserDTO userAccountDTO) {
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
                passwordEncoder.encode(userAccountDTO.getPassword()),
                userAccountDTO.getAvatar(),
                expired,
                locked,
                enabled,
                newUserRoles,
                userAccountDTO.getEmail()
        );
        userAccount = userAccountRepository.save(userAccount);
        UserConfirmationToken userConfirmationToken = createUserConfirmationToken(userAccount);
        sendUserConfirmationToken(userAccount.getEmail(), userConfirmationToken);
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
    @GetMapping(value = Constants.Uls.CONFIRM)
    public String confirm(@RequestParam(Constants.Uls.UUID) UUID uuid) {
        String stringUuid = uuid.toString();
        UserConfirmationToken userConfirmationToken = userConfirmationTokenRepository.findOne(stringUuid);
        if (userConfirmationToken == null) {
            return "redirect:/confirm/registration/token-not-found";
        }

        UserAccount userAccount = userAccountRepository.findOne(userConfirmationToken.getUserId());
        if (userAccount == null) {
            return "redirect:/confirm/registration/user-not-found";
        }
        if (userAccount.isEnabled()) {
            LOGGER.warn("Somebody attempts secondary confirm already confirmed user account with email='{}'", userAccount);
            return Constants.Uls.ROOT;
        }

        userAccount.setEnabled(true);

        userConfirmationTokenRepository.delete(stringUuid);

        return Constants.Uls.ROOT;
    }

    @PostMapping(value = Constants.Uls.API+Constants.Uls.RESEND_CONFIRMATION_EMAIL)
    @ResponseBody
    public void resend(String email) {
        Optional<UserAccount> userAccountOptional = userAccountRepository.findByEmail(email);
        if(!userAccountOptional.isPresent()){
            return; // we care for for email leak
        }
        UserAccount userAccount = userAccountOptional.get();
        if (userAccount.isEnabled()) {
            // this account already confirmed
            return; // we care for for email leak
        }

        UserConfirmationToken userConfirmationToken = createUserConfirmationToken(userAccount);
        sendUserConfirmationToken(email, userConfirmationToken);
    }

    /**
     * https://www.owasp.org/index.php/Forgot_Password_Cheat_Sheet
     * https://stackoverflow.com/questions/1102781/best-way-for-a-forgot-password-implementation/1102817#1102817
     * Yes, if your email is stolen you can lost your account
     * @param email
     */
    @PostMapping(value = Constants.Uls.API+Constants.Uls.REQUEST_PASSWORD_RESET)
    @ResponseBody
    public void requestPasswordReset(String email) {
        UUID uuid = UUID.randomUUID();

        Optional<UserAccount> userAccountOptional = userAccountRepository.findByEmail(email);
        if (!userAccountOptional.isPresent()) {
            return; // we care for for email leak
        }
        UserAccount userAccount = userAccountOptional.get();

        Duration ttl = Duration.ofMinutes(20); // todo
        LocalDateTime expireTime = TimeUtil.getNowUTC().plus(ttl);

        PasswordResetToken passwordResetToken = new PasswordResetToken(uuid, userAccount.getId(), expireTime);

        passwordResetToken = passwordResetTokenRepository.save(passwordResetToken);

        sendPasswordResetToken(userAccount.getEmail(), passwordResetToken);
    }

    public static class PasswordSetDto {
        private UUID passwordResetToken;
        private String newPassword;

        public PasswordSetDto() { }

        public PasswordSetDto(UUID passwordResetToken, String newPassword) {
            this.passwordResetToken = passwordResetToken;
            this.newPassword = newPassword;
        }

        public UUID getPasswordResetToken() {
            return passwordResetToken;
        }

        public void setPasswordResetToken(UUID passwordResetToken) {
            this.passwordResetToken = passwordResetToken;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }

    @PostMapping(value = Constants.Uls.API + Constants.Uls.PASSWORD_RESET_SET_NEW)
    @ResponseBody
    public void resetPassword(@RequestBody @Valid PasswordSetDto passwordSetDto) {

        // webpage parses token uuid from URL
        // .. and js sends this request

        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findOne(passwordSetDto.getPasswordResetToken());
        if (passwordResetToken == null) {
            throw new PasswordResetTokenNotFoundException("password reset token not found");
        }
        if (TimeUtil.getNowUTC().isAfter(passwordResetToken.getExpiredAt()) ) {
            throw new PasswordResetTokenNotFoundException("password reset token is expired");
        }
        Optional<UserAccount> userAccountOptional = userAccountRepository.findById(passwordResetToken.getUserId());
        if(!userAccountOptional.isPresent()) {
            return;
        }

        UserAccount userAccount = userAccountOptional.get();

        userAccount.setPassword(passwordEncoder.encode(passwordSetDto.getNewPassword()));

        return;
    }


}
