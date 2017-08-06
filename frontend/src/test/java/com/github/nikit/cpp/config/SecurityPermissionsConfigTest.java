package com.github.nikit.cpp.config;

import com.github.nikit.cpp.entity.jpa.UserRole;
import com.github.nikit.cpp.security.SecurityPermissionsConfig;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collection;
import java.util.Collections;

public class SecurityPermissionsConfigTest {

    @Test
    public void testAdminCanBeUser() throws Exception {
        SecurityPermissionsConfig securityPermissionsConfig = new SecurityPermissionsConfig();
        Collection<GrantedAuthority> roles = Collections.singletonList(new SimpleGrantedAuthority(UserRole.ROLE_ADMIN.name()));
        java.util.Collection<? extends GrantedAuthority> reachable = securityPermissionsConfig.roleHierarchy().getReachableGrantedAuthorities(roles);

        Assert.assertEquals(3, reachable.size());
        Assert.assertTrue(reachable.contains(new SimpleGrantedAuthority(UserRole.ROLE_ADMIN.name())));
        Assert.assertTrue(reachable.contains(new SimpleGrantedAuthority(UserRole.ROLE_MODERATOR.name())));
        Assert.assertTrue(reachable.contains(new SimpleGrantedAuthority(UserRole.ROLE_USER.name())));
    }
}
