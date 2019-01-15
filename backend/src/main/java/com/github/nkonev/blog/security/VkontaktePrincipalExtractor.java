package com.github.nkonev.blog.security;

import com.github.nkonev.blog.converter.UserAccountConverter;
import com.github.nkonev.blog.dto.UserAccountDetailsDTO;
import com.github.nkonev.blog.entity.jpa.UserAccount;
import com.github.nkonev.blog.exception.OauthIdConflictException;
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
public class VkontaktePrincipalExtractor extends AbstractPrincipalExtractor implements PrincipalExtractor {

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

        UserAccountDetailsDTO mergedPrincipal = mergeOauthIdToExistsUser(vkontakteId);
        if (mergedPrincipal != null) {
            return mergedPrincipal;
        }

        String login = getLogin(m);

        return createOrGetExistsUser(vkontakteId, login, map);
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
        return login;
    }

    @Override
    protected Logger logger() {
        return LOGGER;
    }

    @Override
    protected String getOauthName() {
        return "vkontate";
    }

    @Override
    protected Optional<UserAccount> findByOauthId(String oauthId) {
        return userAccountRepository.findByOauthIdentifiersVkontakteId(oauthId);
    }

    @Override
    protected void setOauthIdToPrincipal(UserAccountDetailsDTO principal, String oauthId) {
        principal.getOauthIdentifiers().setVkontakteId(oauthId);
    }

    @Override
    protected void setOauthIdToEntity(Long id, String oauthId) {
        UserAccount userAccount = userAccountRepository.findById(id).orElseThrow();
        userAccount.getOauthIdentifiers().setVkontakteId(oauthId);
    }

    @Override
    protected UserAccount saveEntity(String oauthId, String login, Map<String, Object> oauthResourceServerResponse) {
        UserAccount userAccount = UserAccountConverter.buildUserAccountEntityForVkontakteInsert(oauthId, login);
        userAccount = userAccountRepository.save(userAccount);

        return userAccount;

    }

    @Override
    protected String getLoginPrefix() {
        return LOGIN_PREFIX;
    }

    @Override
    protected Optional<UserAccount> findByUsername(String login) {
        return userAccountRepository.findByUsername(login);
    }
}
