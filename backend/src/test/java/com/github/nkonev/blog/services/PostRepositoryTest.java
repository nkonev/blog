package com.github.nkonev.blog.services;

import com.github.nkonev.blog.AbstractUtTestRunner;
import com.github.nkonev.blog.entity.jpa.Post;
import com.github.nkonev.blog.repo.jpa.PostRepository;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import javax.validation.ConstraintViolationException;

public class PostRepositoryTest extends AbstractUtTestRunner {
    @Autowired
    private PostRepository postRepository;

    private Post target;

    @BeforeEach
    public void beforePostRepositoryTest() {
        target = postRepository.findById(50L).orElseThrow(()->new IllegalArgumentException("test post not found"));
    }

    @org.junit.jupiter.api.Test
    public void testLeft() throws Exception {
        Assert.assertNotNull(postRepository.getLeft(target.getId()));
    }

    @Test
    public void testRight() throws Exception {
        Assert.assertNotNull(postRepository.getRight(target.getId()));
    }

    @org.junit.jupiter.api.Test
    public void testLeftNull() throws Exception {
        Assertions.assertThrows(ConstraintViolationException.class, () -> {
            postRepository.getLeft(null);
        });
    }

    @Test
    public void testRightNull() throws Exception {
        Assertions.assertThrows(ConstraintViolationException.class,  () -> {
            postRepository.getRight(null);
        });
    }

    @Test
    public void testLeftNone() throws Exception {
        Assert.assertNull(postRepository.getLeft(-50_000L));
    }

    @Test
    public void testRightNone() throws Exception {
        Assert.assertNull(postRepository.getRight(50_000L));
    }
}
