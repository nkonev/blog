package com.github.nikit.cpp.security;

import com.github.nikit.cpp.dto.CommentDTO;
import com.github.nikit.cpp.dto.PostDTO;
import com.github.nikit.cpp.dto.UserAccountDetailsDTO;
import com.github.nikit.cpp.entity.jpa.Comment;
import com.github.nikit.cpp.entity.jpa.Permissions;
import com.github.nikit.cpp.entity.jpa.Post;
import com.github.nikit.cpp.entity.jpa.UserRole;
import com.github.nikit.cpp.repo.jpa.CommentRepository;
import com.github.nikit.cpp.repo.jpa.PostRepository;
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

    public boolean hasPostPermission(PostDTO dto, UserAccountDetailsDTO userAccount, Permissions permission) {
        Assert.notNull(dto, "PostDTO can't be null");
        return hasPostPermission(dto.getId(), userAccount, permission);
    }

    private Post getPostOrException(long id) {
        Post post = postRepository.findOne(id);
        Assert.notNull(post, "Post with id "+id+" not found");
        return post;
    }

    public boolean hasPostPermission(long id, UserAccountDetailsDTO userAccount, Permissions permission) {
        Post post = getPostOrException(id);
        return hasPostPermission(post, userAccount, permission);
    }

    public boolean hasPostPermission(Post post, UserAccountDetailsDTO userAccount, Permissions permission) {
        Assert.notNull(post, "post can't be null");
        if (userAccount == null) {return false;}
        if (roleHierarchy.getReachableGrantedAuthorities(userAccount.getAuthorities()).contains(new SimpleGrantedAuthority(UserRole.ROLE_MODERATOR.name()))){
            return true;
        }
        if (post.getOwner().getId().equals(userAccount.getId())){
            if (permission==Permissions.DELETE) { return false; }
            return true;
        }
        return false;
    }





    public boolean hasCommentPermission(CommentDTO dto, UserAccountDetailsDTO userAccount, Permissions permission) {
        Assert.notNull(dto, "CommentDTO can't be null");
        return hasCommentPermission(dto.getId(), userAccount, permission);
    }

    public boolean hasCommentPermission(long id, UserAccountDetailsDTO userAccount, Permissions permission) {
        Comment comment = commentRepository.findOne(id);
        Assert.notNull(comment, "Comment with id "+id+" not found");
        return hasCommentPermission(comment, userAccount, permission);
    }

    public boolean hasCommentPermission(Comment comment, UserAccountDetailsDTO userAccount, Permissions permission) {
        Assert.notNull(comment, "comment can't be null");
        if (userAccount == null) {return false;}
        if (roleHierarchy.getReachableGrantedAuthorities(userAccount.getAuthorities()).contains(new SimpleGrantedAuthority(UserRole.ROLE_MODERATOR.name()))){
            return true;
        }
        if (comment.getOwner().getId().equals(userAccount.getId())){
            return true;
        }

        return false;
    }

}
