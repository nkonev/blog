package com.github.nkonev.blog.services;

import com.github.nkonev.blog.AbstractUtTestRunner;
import com.github.nkonev.blog.entity.jpa.Post;
import com.github.nkonev.blog.repo.jpa.PostRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import javax.validation.ConstraintViolationException;

public class PostRepositoryTest extends AbstractUtTestRunner {
    @Autowired
    private PostRepository postRepository;

    private Post target;

    @Before
    public void beforePostRepositoryTest() {
        target = postRepository.findById(50L).orElseThrow(()->new IllegalArgumentException("test post not found"));
    }

    @Test
    public void testLeft() throws Exception {
        Assert.assertNotNull(postRepository.getLeft(target.getId()));
    }

    @Test
    public void testRight() throws Exception {
        Assert.assertNotNull(postRepository.getRight(target.getId()));
    }

    @Test(expected = ConstraintViolationException.class)
    public void testLeftNull() throws Exception {
        postRepository.getLeft(null);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testRightNull() throws Exception {
        postRepository.getRight(null);
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
