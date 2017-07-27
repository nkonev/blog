package com.github.nikit.cpp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.*;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.sql.DataSource;

// https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#domain-acls
// https://stackoverflow.com/questions/26292431/how-to-configure-spring-acl-without-xml-file
// gotcha https://stackoverflow.com/questions/38609874/acl-security-in-spring-boot
// http://book2s.com/java/src/package/com/foreach/across/modules/spring/security/acl/config/aclsecurityconfiguration.html

/**
 * tables in "auth" schema
 * ACL_SID              GrantedAuthority(ROLE_ADMIN, ...) or Principal
 * ACL_CLASS            Post.class or Comment.class
 * ACL_OBJECT_IDENTITY  acl_class_id, parent_acl_sid_id(owner) Post or Comment instance
 * ACL_ENTRY            acl_object_identity_id, acl_sid_id(recipient), auditing, permissions_bitmask_integer
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@EnableCaching
public class SecurityAclConfig {
    @Autowired
    @Qualifier(value = DbConfig.AUTH_DATASOURCE_BEAN_NAME)
    private DataSource dataSource;

    public static final String CACHE_NAME = "acl-cache";

    @Autowired
    private CacheManager cacheManager;

    private Cache cacheInstance() {
        return cacheManager.getCache(CACHE_NAME);
    }

    @Bean
    public PermissionGrantingStrategy permissionGrantingStrategy() {
        return new DefaultPermissionGrantingStrategy(new ConsoleAuditLogger());
    }

    @Bean
    AclCache aclCache() {
        return new SpringCacheBasedAclCache(cacheInstance(), permissionGrantingStrategy(), aclAuthorizationStrategy());
    }

    /**
     * https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#domain-acls-key-concepts
     * LookupStrategy provides a highly optimized strategy for retrieving ACL information, using batched retrievals
     * @return
     */
    @Bean
    LookupStrategy lookupStrategy() {
        return new BasicLookupStrategy(dataSource, aclCache(), aclAuthorizationStrategy(), new ConsoleAuditLogger());
    }


    /**
     * As I see this is who besides owner can change ACL
     * @return
     */
    AclAuthorizationStrategy aclAuthorizationStrategy() {
        return new AclAuthorizationStrategyImpl(
//                new SimpleGrantedAuthority("ROLE_ADMIN"),
//                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority(SecurityConfig.ROLE_ADMIN)
        );
    }

    /**
     * https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#domain-acls-key-concepts
     * Retrieves the Acl applicable for a given ObjectIdentity. Acl internally holds [ManyToOne] ObjectIdentity. And List of AccessControlEntry
     * Acl <==> Secured object instance
     * @return
     */
    @Bean
    JdbcMutableAclService aclService() {
        JdbcMutableAclService service = new JdbcMutableAclService(dataSource, lookupStrategy(), aclCache());
        // https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#postgresql
        service.setClassIdentityQuery("select currval(pg_get_serial_sequence('acl_class', 'id'))");
        service.setSidIdentityQuery("select currval(pg_get_serial_sequence('acl_sid', 'id'))");
        return service;
    }

    // @Override
    @Bean
    protected MethodSecurityExpressionHandler createExpressionHandler(){
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(new AclPermissionEvaluator(aclService()));
        return expressionHandler;
    }
}
