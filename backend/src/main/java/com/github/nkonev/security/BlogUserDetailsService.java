package com.github.nkonev.security;

import com.github.nkonev.converter.UserAccountConverter;
import com.github.nkonev.dto.UserAccountDetailsDTO;
import com.github.nkonev.entity.jpa.UserAccount;
import com.github.nkonev.exception.DataNotFoundException;
import com.github.nkonev.repo.jpa.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.session.ExpiringSession;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * Provides Spring Security compatible UserAccountDetailsDTO.
 */
@Component
public class BlogUserDetailsService implements UserDetailsService {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private RedisOperationsSessionRepository redisOperationsSessionRepository;

    /**
     * load UserAccountDetailsDTO from database, or throws UsernameNotFoundException
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserAccountDetailsDTO loadUserByUsername(String username) throws UsernameNotFoundException {
        return userAccountRepository
                .findByUsername(username)
                .map(UserAccountConverter::convertToUserAccountDetailsDTO)
                .orElseThrow(() -> new UsernameNotFoundException("User with login '" + username + "' not found"));
    }

    /**
     * Set new UserDetails to SecurityContext.
     * When spring mvc finishes request processing, UserDetails will be stored in Session and effectively appears in Redis
     * @param userAccount
     */
    public void refreshUserDetails(UserAccount userAccount) {
        Assert.notNull(userAccount, "userAccount cannot be null");
        UserAccountDetailsDTO newUd = UserAccountConverter.convertToUserAccountDetailsDTO(userAccount);
        Authentication authentication = new UsernamePasswordAuthenticationToken(newUd, newUd.getPassword(), newUd.getAuthorities());
        Assert.notNull(SecurityContextHolder.getContext(), "securityContext cannot be null");
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Map<String, ExpiringSession> getSessions(String userName){
        Object o = redisOperationsSessionRepository.findByIndexNameAndIndexValue(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, userName);
        return (Map<String, ExpiringSession>)o;
    }

    public Map<String, ExpiringSession> getMySessions(UserDetails userDetails){
        if (userDetails == null){
            throw new RuntimeException("getMySessions may be called only by authorized users");
        }
        return getSessions(userDetails.getUsername());
    }

    public UserAccount getUserAccount(long userId){
        return userAccountRepository.findById(userId).orElseThrow(() -> new DataNotFoundException("User with id " + userId + " not found"));
    }

    public void killSessions(long userId){
        String userName = getUserAccount(userId).getUsername();
        Map<String, ExpiringSession> sessionMap = getSessions(userName);
        sessionMap.keySet().forEach(session -> redisOperationsSessionRepository.delete(session));
    }

    public void killSessions(String userName){
        Map<String, ExpiringSession> sessionMap = getSessions(userName);
        sessionMap.keySet().forEach(session -> redisOperationsSessionRepository.delete(session));
    }

    public Map<String, ExpiringSession> getSessions(long userId) {
        UserAccount userAccount = getUserAccount(userId);
        return getSessions(userAccount.getUsername());
    }
}
