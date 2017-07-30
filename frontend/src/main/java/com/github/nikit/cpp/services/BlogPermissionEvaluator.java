package com.github.nikit.cpp.services;

import com.github.nikit.cpp.dto.PostDTO;
import com.github.nikit.cpp.dto.UserAccountDetailsDTO;
import com.github.nikit.cpp.entity.Permissions;
import com.github.nikit.cpp.entity.Post;
import com.github.nikit.cpp.entity.UserRole;
import com.github.nikit.cpp.repo.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.Serializable;

@Service
public class BlogPermissionEvaluator implements PermissionEvaluator {

    @Autowired
    private PostRepository postRepository;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (!authentication.isAuthenticated()){ return false; }
        Permissions perm = (Permissions) permission;
        UserAccountDetailsDTO userAccountDetailsDTO = (UserAccountDetailsDTO) authentication.getPrincipal();

        if (targetDomainObject instanceof PostDTO) {
            return hasPostPermission((PostDTO) targetDomainObject, userAccountDetailsDTO, perm);
        }
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean hasPostPermission(PostDTO dto, UserAccountDetailsDTO userAccount, Permissions permission) {
        Assert.notNull(dto, "PostDTO can't be null");
        Post post = postRepository.findOne(dto.getId());
        Assert.notNull(post, "Post with id "+dto.getId()+" not found");
        return hasPostPermission(post, userAccount, permission);
    }

    public boolean hasPostPermission(Post saved, UserAccountDetailsDTO userAccount, Permissions permission) {
        if (userAccount == null) {return false;}

        if (saved.getOwner().getId().equals(userAccount.getId())){
            return true;
        }
        if (userAccount.getAuthorities().contains(new SimpleGrantedAuthority(UserRole.ROLE_ADMIN.name()))){
            return true;
        }
        return false;
    }
}
