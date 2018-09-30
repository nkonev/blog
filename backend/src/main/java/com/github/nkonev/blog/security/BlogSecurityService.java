package com.github.nkonev.blog.security;

import com.github.nkonev.blog.dto.CommentDTO;
import com.github.nkonev.blog.dto.PostDTO;
import com.github.nkonev.blog.dto.UserAccountDetailsDTO;
import com.github.nkonev.blog.entity.jpa.Comment;
import com.github.nkonev.blog.entity.jpa.Post;
import com.github.nkonev.blog.entity.jpa.UserRole;
import com.github.nkonev.blog.repo.jpa.CommentRepository;
import com.github.nkonev.blog.repo.jpa.PostRepository;
import com.github.nkonev.blog.security.permissions.CommentPermissions;
import com.github.nkonev.blog.security.permissions.PostPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Optional;

/**
 * Central entrypoint for access decisions
 */
@Service
public class BlogSecurityService {
    @Autowired
    private RoleHierarchy roleHierarchy;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    public boolean hasPostPermission(PostDTO dto, UserAccountDetailsDTO userAccount, PostPermissions permission) {
        Assert.notNull(dto, "PostDTO can't be null");
        return hasPostPermission(dto.getId(), userAccount, permission);
    }

    private Post getPostOrException(long id) {
        Post post = postRepository.findById(id).orElseThrow(()->new IllegalArgumentException("Post with id "+id+" not found"));
        return post;
    }

    public boolean hasPostPermission(long id, UserAccountDetailsDTO userAccount, PostPermissions permission) {
        Post post = getPostOrException(id);
        return hasPostPermission(post, userAccount, permission);
    }

    public boolean hasPostPermission(UserAccountDetailsDTO userAccount, PostPermissions permission) {
        return hasPostPermission((Post)null, userAccount, permission);
    }


    public boolean hasPostPermission(Post post, UserAccountDetailsDTO userAccount, PostPermissions permission) {
        if (userAccount == null) {return false;}

        if (permission == PostPermissions.CREATE) {
            return true;
        }

        if (permission == PostPermissions.READ_MY) {
            return true;
        }

        if (post == null) {
            return false;
        }

        if (roleHierarchy.getReachableGrantedAuthorities(userAccount.getAuthorities()).contains(new SimpleGrantedAuthority(UserRole.ROLE_MODERATOR.name()))){
            return true;
        }
        if (post.getOwner().getId().equals(userAccount.getId()) && permission==PostPermissions.EDIT){
            return true;
        }
        return false;
    }





    public boolean hasCommentPermission(CommentDTO dto, UserAccountDetailsDTO userAccount, CommentPermissions permission) {
        Assert.notNull(dto, "CommentDTO can't be null");
        return hasCommentPermission(dto.getId(), userAccount, permission);
    }

    public boolean hasCommentPermission(long id, UserAccountDetailsDTO userAccount, CommentPermissions permission) {
        Comment comment = commentRepository.findById(id).orElseThrow(()->new IllegalArgumentException("Comment with id "+id+" not found"));
        return hasCommentPermission(comment, userAccount, permission);
    }

    public boolean hasCommentPermission(UserAccountDetailsDTO userAccount, CommentPermissions permission) {
        return hasCommentPermission((Comment)null, userAccount, permission);
    }

    public boolean hasCommentPermission(Comment comment, UserAccountDetailsDTO userAccount, CommentPermissions permission) {
        if (userAccount == null) {
            return false;
        }

        if (permission == CommentPermissions.CREATE) {
            return true;
        }

        if (comment == null) {
            return false;
        }

        if (roleHierarchy.getReachableGrantedAuthorities(userAccount.getAuthorities()).contains(new SimpleGrantedAuthority(UserRole.ROLE_MODERATOR.name()))){
            return true;
        }
        if (comment.getOwner().getId().equals(userAccount.getId())){
            return true;
        }

        return false;
    }

    public boolean hasSessionManagementPermission(UserAccountDetailsDTO userAccount) {
        if (userAccount==null){
            return false;
        }
        if (roleHierarchy.getReachableGrantedAuthorities(userAccount.getAuthorities()).contains(new SimpleGrantedAuthority(UserRole.ROLE_MODERATOR.name()))){
            return true;
        } else {
            return false;
        }
    }

    public boolean canLock(UserAccountDetailsDTO userAccount) {
        if (userAccount==null){
            return false;
        }
        if (roleHierarchy.getReachableGrantedAuthorities(userAccount.getAuthorities()).contains(new SimpleGrantedAuthority(UserRole.ROLE_MODERATOR.name()))){
            return true;
        } else {
            return false;
        }
    }

    public boolean hasUserManagementPermission(UserAccountDetailsDTO userAccount) {
        return hasSessionManagementPermission(userAccount);
    }

    public boolean hasSettingsPermission(UserAccountDetailsDTO userAccount) {
        return Optional
                .ofNullable(userAccount)
                .map(u -> u.getAuthorities()
                        .contains(new SimpleGrantedAuthority(UserRole.ROLE_ADMIN.name())))
                .orElse(false);
    }
}
