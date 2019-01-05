package com.github.nkonev.blog.security;

import com.github.nkonev.blog.converter.UserAccountConverter;
import com.github.nkonev.blog.entity.jpa.UserAccount;
import com.github.nkonev.blog.repo.jpa.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
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
