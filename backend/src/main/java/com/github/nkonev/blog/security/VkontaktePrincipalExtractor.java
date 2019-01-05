package com.github.nkonev.blog.security;

import com.github.nkonev.blog.converter.UserAccountConverter;
import com.github.nkonev.blog.dto.UserAccountDetailsDTO;
import com.github.nkonev.blog.entity.jpa.UserAccount;
import com.github.nkonev.blog.exception.UserAlreadyPresentException;
import com.github.nkonev.blog.repo.jpa.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Transactional
public class VkontaktePrincipalExtractor implements PrincipalExtractor {

    @Autowired
    private UserAccountRepository userAccountRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(VkontaktePrincipalExtractor.class);

    public static final String LOGIN_PREFIX = "vkontakte_";

    @Override
    public Object extractPrincipal(Map<String, Object> map) {
        List l = (List) map.get("response");
        Map<String, Object> m = (Map<String, Object>) l.get(0);

        String vkontakteId = getId(m);
        Assert.notNull(vkontakteId, "vkontakteId cannot be null");

        if (SecurityContextHolder.getContext().getAuthentication()!=null && SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof UserAccountDetailsDTO) {
            // we already authenticated - so it' s binding
            UserAccountDetailsDTO principal = (UserAccountDetailsDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            LOGGER.info("Will merge vkontakteId to exists user '{}', id={}", principal.getUsername(), principal.getId());

            Optional<UserAccount> maybeUserAccount = userAccountRepository.findByOauthIdentifiersVkontakteId(vkontakteId);
            if (maybeUserAccount.isPresent() && !maybeUserAccount.get().getId().equals(principal.getId())){
                LOGGER.error("With vkontakteId={} already present another user '{}', id={}", vkontakteId, maybeUserAccount.get().getUsername(), maybeUserAccount.get().getId());
                throw new UserAlreadyPresentException();
            }

            principal.getOauthIdentifiers().setVkontakteId(vkontakteId);

            UserAccount userAccount = userAccountRepository.findById(principal.getId()).orElseThrow();
            userAccount.getOauthIdentifiers().setVkontakteId(vkontakteId);
            LOGGER.info("vkontakteId successfully merged to exists user '{}', id={}", principal.getUsername(), principal.getId());
            return principal;
        }

        UserAccount userAccount;
        Optional<UserAccount> userAccountOpt = userAccountRepository.findByOauthIdentifiersVkontakteId(vkontakteId);
        if (!userAccountOpt.isPresent()){
            String login = getLogin(m);
            userAccount = UserAccountConverter.buildUserAccountEntityForVkontakteInsert(vkontakteId, login);
            userAccount = userAccountRepository.save(userAccount);
        } else {
            userAccount = userAccountOpt.get();
        }

        return UserAccountConverter.convertToUserAccountDetailsDTO(userAccount);
    }

    private String getId(Map<String, Object> m) {
        return ((Integer) m.get("id")).toString();
    }

    private String getLogin(Map<String, Object> map) {
        String firstName = (String) map.get("first_name");
        String lastName = (String) map.get("last_name");
        String login = "";
        if (firstName!=null) {
            firstName = firstName.trim();
            login += firstName;
            login += " ";
        }
        if (lastName!=null) {
            lastName = lastName.trim();
            login += lastName;
        }
        Assert.hasLength(login, "vkontakte name cannot be null");
        login = login.trim();
        String vkontakteId = getId(map);
        if (userAccountRepository.findByUsername(login).isPresent()){
            LOGGER.info("User with login '{}' already present in database, so we' ll generate login", login);
            return LOGIN_PREFIX+vkontakteId;
        } else {
            return login;
        }
    }
}
