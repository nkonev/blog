package com.github.nkonev.blog.security;

import com.github.nkonev.blog.controllers.AbstractImageUploadController;
import com.github.nkonev.blog.controllers.ImageUserAvatarUploadController;
import com.github.nkonev.blog.converter.UserAccountConverter;
import com.github.nkonev.blog.dto.UserAccountDetailsDTO;
import com.github.nkonev.blog.entity.jpa.UserAccount;
import com.github.nkonev.blog.exception.OauthIdConflictException;
import com.github.nkonev.blog.exception.UserAlreadyPresentException;
import com.github.nkonev.blog.repo.jpa.UserAccountRepository;
import com.github.nkonev.blog.utils.ImageDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

@Component
@Transactional
public class FacebookPrincipalExtractor extends AbstractPrincipalExtractor implements PrincipalExtractor {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private ImageUserAvatarUploadController imageUserAvatarUploadController;

    @Autowired
    private ImageDownloader imageDownloader;

    private static final Logger LOGGER = LoggerFactory.getLogger(FacebookPrincipalExtractor.class);

    public static final String LOGIN_PREFIX = "facebook_";

    private String getAvatarUrl(Map<String, Object> map){
        try {
            String url = (String) ((Map<String, Object>) ((Map<String, Object>) map.get("picture")).get("data")).get("url");
            return imageDownloader.downloadImageAndSave(url, imageUserAvatarUploadController);
        } catch (Exception e){
            LOGGER.info("Cannot get image url from {}, returning null", map);
            return null;
        }
    }

    @Override
    public Object extractPrincipal(Map<String, Object> map) {
        String facebookId = getId(map);
        Assert.notNull(facebookId, "facebookId cannot be null");

        UserAccountDetailsDTO mergedPrincipal = mergeOauthIdToExistsUser(facebookId);
        if (mergedPrincipal != null) {
            return mergedPrincipal;
        }

        String login = getLogin(map);

        return createOrGetExistsUser(facebookId, login, map);
    }

    private String getLogin(Map<String, Object> map) {
        String login = (String) map.get("name");
        Assert.hasLength(login, "facebook name cannot be null");
        login = login.trim();
        login = login.replaceAll(" +", " ");
        return login;
    }

    private String getId(Map<String, Object> map) {
        return (String) map.get("id");
    }

    @Override
    protected Logger logger() {
        return LOGGER;
    }

    @Override
    protected String getOauthName() {
        return "facebook";
    }

    @Override
    protected Optional<UserAccount> findByOauthId(String oauthId) {
        return userAccountRepository.findByOauthIdentifiersFacebookId(oauthId);
    }

    @Override
    protected void setOauthIdToPrincipal(UserAccountDetailsDTO principal, String oauthId) {
        principal.getOauthIdentifiers().setFacebookId(oauthId);
    }

    @Override
    protected void setOauthIdToEntity(Long id, String oauthId) {
        UserAccount userAccount = userAccountRepository.findById(id).orElseThrow();
        userAccount.getOauthIdentifiers().setFacebookId(oauthId);
    }

    @Override
    protected UserAccount saveEntity(String oauthId, String login, Map<String, Object> map) {
        String maybeImageUrl = getAvatarUrl(map);

        UserAccount userAccount = UserAccountConverter.buildUserAccountEntityForFacebookInsert(oauthId, login, maybeImageUrl);
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
