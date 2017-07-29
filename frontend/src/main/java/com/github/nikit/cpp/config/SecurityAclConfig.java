package com.github.nikit.cpp.config;

import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.entity.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.*;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.AuditableAccessControlEntry;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.Assert;

import javax.sql.DataSource;

// https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#domain-acls
// https://stackoverflow.com/questions/26292431/how-to-configure-spring-acl-without-xml-file
// gotcha https://stackoverflow.com/questions/38609874/acl-security-in-spring-boot
// http://book2s.com/java/src/package/com/foreach/across/modules/spring/security/acl/config/aclsecurityconfiguration.html

/**
 * tables in ""+SCHEMA+"" schema
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
    private DataSource dataSource;

    public static final String CACHE_NAME = "acl-cache";
    
    private static final String SCHEMA = Constants.Schemas.AUTH;

    @Autowired
    private CacheManager cacheManager;

    private Cache cacheInstance() {
        return cacheManager.getCache(CACHE_NAME);
    }

    public static class Slf4jAuditLogger implements AuditLogger {

        private static final Logger LOGGER = LoggerFactory.getLogger(Slf4jAuditLogger.class);

        @Override
        public void logIfNeeded(boolean granted, AccessControlEntry ace) {
            Assert.notNull(ace, "AccessControlEntry required");

            if (ace instanceof AuditableAccessControlEntry) {
                AuditableAccessControlEntry auditableAce = (AuditableAccessControlEntry) ace;

                if (granted && auditableAce.isAuditSuccess()) {
                    LOGGER.info("GRANTED due to ACE: " + ace);
                }
                else if (!granted && auditableAce.isAuditFailure()) {
                    LOGGER.info("DENIED due to ACE: " + ace);
                }
            }
        }
    }


    @Bean
    public PermissionGrantingStrategy permissionGrantingStrategy() {
        return new DefaultPermissionGrantingStrategy(new Slf4jAuditLogger());
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
        return new BasicLookupStrategy(dataSource, aclCache(), aclAuthorizationStrategy(), new Slf4jAuditLogger());
    }


    /**
     * As I see this is who besides owner can change ACL
     * @return
     */
    AclAuthorizationStrategy aclAuthorizationStrategy() {
        return new AclAuthorizationStrategyImpl(new SimpleGrantedAuthority(UserRole.ROLE_ADMIN.name()));
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
        service.setClassIdentityQuery("select currval(pg_get_serial_sequence('"+SCHEMA+".acl_class', 'id'))");
        service.setSidIdentityQuery("select currval(pg_get_serial_sequence('"+SCHEMA+".acl_sid', 'id'))");
        
        // just set schema
        service.setClassPrimaryKeyQuery("select id from "+SCHEMA+".acl_class where class=?");
        service.setDeleteEntryByObjectIdentityForeignKeySql("delete from "+SCHEMA+".acl_entry where acl_object_identity=?");
        service.setDeleteObjectIdentityByPrimaryKeySql("delete from "+SCHEMA+".acl_object_identity where id=?");
        service.setInsertClassSql("insert into "+SCHEMA+".acl_class (class) values (?)");
        service.setInsertEntrySql("insert into "+SCHEMA+".acl_entry "
                + "(acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure)"
                + "values (?, ?, ?, ?, ?, ?, ?)");
        service.setInsertObjectIdentitySql("insert into "+SCHEMA+".acl_object_identity "
                + "(object_id_class, object_id_identity, owner_sid, entries_inheriting) "
                + "values (?, ?, ?, ?)");
        service.setInsertSidSql("insert into "+SCHEMA+".acl_sid (principal, sid) values (?, ?)");
        service.setObjectIdentityPrimaryKeyQuery("select "+SCHEMA+".acl_object_identity.id from "+SCHEMA+".acl_object_identity, "+SCHEMA+".acl_class "
                + "where "+SCHEMA+".acl_object_identity.object_id_class = "+SCHEMA+".acl_class.id and "+SCHEMA+".acl_class.class=? "
                + "and "+SCHEMA+".acl_object_identity.object_id_identity = ?");
        service.setSidPrimaryKeyQuery("select id from "+SCHEMA+".acl_sid where principal=? and sid=?");
        service.setUpdateObjectIdentity("update "+SCHEMA+".acl_object_identity set "
                + "parent_object = ?, owner_sid = ?, entries_inheriting = ?"
                + " where id = ?");
        
        service.setFindChildrenQuery("select obj.object_id_identity as obj_id, class.class as class "
                + "from "+SCHEMA+".acl_object_identity obj, "+SCHEMA+".acl_object_identity parent, "+SCHEMA+".acl_class class "
                + "where obj.parent_object = parent.id and obj.object_id_class = class.id "
                + "and parent.object_id_identity = ? and parent.object_id_class = ("
                + "select id FROM "+SCHEMA+".acl_class where "+SCHEMA+".acl_class.class = ?)");
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
