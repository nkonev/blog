package com.github.nkonev.security;

import com.github.nkonev.dto.CommentDTO;
import com.github.nkonev.dto.PostDTO;
import com.github.nkonev.dto.UserAccountDetailsDTO;
import com.github.nkonev.entity.jpa.Comment;
import com.github.nkonev.entity.jpa.Post;
import com.github.nkonev.entity.jpa.UserRole;
import com.github.nkonev.repo.jpa.CommentRepository;
import com.github.nkonev.repo.jpa.PostRepository;
import com.github.nkonev.security.permissions.CommentPermissions;
import com.github.nkonev.security.permissions.PostPermissions;
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

    @Autowired
    private CommentRepository commentRepository;

    public boolean hasPostPermission(PostDTO dto, UserAccountDetailsDTO userAccount, PostPermissions permission) {
        Assert.notNull(dto, "PostDTO can't be null");
        return hasPostPermission(dto.getId(), userAccount, permission);
    }

    private Post getPostOrException(long id) {
        Post post = postRepository.findOne(id);
        Assert.notNull(post, "Post with id "+id+" not found");
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
        Comment comment = commentRepository.findOne(id);
        Assert.notNull(comment, "Comment with id "+id+" not found");
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

}
