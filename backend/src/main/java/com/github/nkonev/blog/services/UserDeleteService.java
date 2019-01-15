package com.github.nkonev.blog.services;

import com.github.nkonev.blog.Constants;
import com.github.nkonev.blog.entity.jpa.UserAccount;
import com.github.nkonev.blog.repo.jpa.CommentRepository;
import com.github.nkonev.blog.repo.jpa.PostRepository;
import com.github.nkonev.blog.repo.jpa.UserAccountRepository;
import com.github.nkonev.blog.security.BlogSecurityService;
import com.github.nkonev.blog.security.BlogUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserDeleteService {
    @Autowired
    private BlogUserDetailsService blogUserDetailsService;

    @Autowired
    private BlogSecurityService blogSecurityService;

    @Autowired
    private PostService postService;

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PostRepository postRepository;

    public long deleteUser(long userId) {
        blogUserDetailsService.killSessions(userId);
        UserAccount deleted = userAccountRepository.findByUsername(Constants.DELETED).orElseThrow();
        postRepository.moveToAnotherUser(userId, deleted.getId());
        commentRepository.moveToAnotherUser(userId, deleted.getId());
        userAccountRepository.deleteById(userId);

        return userAccountRepository.count();
    }

}
