package com.github.nikit.cpp.services;

import com.github.nikit.cpp.dto.PostDTO;
import com.github.nikit.cpp.dto.UserAccountDetailsDTO;
import com.github.nikit.cpp.entity.Permissions;
import com.github.nikit.cpp.entity.Post;
import com.github.nikit.cpp.entity.UserRole;
import com.github.nikit.cpp.repo.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Central entrypoint for access decisions
 */
@Service
public class BlogSecurityService {
    @Autowired
    private RoleHierarchy roleHierarchy;

    @Autowired
    private PostRepository postRepository;


    public boolean hasPostPermission(PostDTO dto, UserAccountDetailsDTO userAccount, Permissions permission) {
        Assert.notNull(dto, "PostDTO can't be null");
        Post post = postRepository.findOne(dto.getId());
        Assert.notNull(post, "Post with id "+dto.getId()+" not found");
        return hasPostPermission(post, userAccount, permission);
    }

    public boolean hasPostPermission(long id, UserAccountDetailsDTO userAccount, Permissions permission) {
        Post post = postRepository.findOne(id);
        Assert.notNull(post, "Post with id "+id+" not found");
        return hasPostPermission(post, userAccount, permission);
    }

    public boolean hasPostPermission(Post saved, UserAccountDetailsDTO userAccount, Permissions permission) {
        if (userAccount == null) {return false;}
        if (roleHierarchy.getReachableGrantedAuthorities(userAccount.getAuthorities()).contains(new SimpleGrantedAuthority(UserRole.ROLE_MODERATOR.name()))){
            return true;
        }
        if (saved.getOwner().getId().equals(userAccount.getId())){
            if (permission==Permissions.DELETE) { return false; }
            return true;
        }
        return false;
    }

}
