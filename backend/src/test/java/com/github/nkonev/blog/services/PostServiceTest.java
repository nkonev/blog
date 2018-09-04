package com.github.nkonev.blog.services;

import com.github.nkonev.blog.AbstractUtTestRunner;
import com.github.nkonev.blog.entity.elasticsearch.Post;
import com.github.nkonev.blog.repo.elasticsearch.IndexPostRepository;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PostServiceTest extends AbstractUtTestRunner {

    @Autowired
    private IndexPostRepository indexPostRepository;

    @Autowired
    private PostService postService;

    @Test
    public void testCleanOldElasticsearchGarbage(){
        Post post = new Post();
        post.setId(20000L);
        post.setText("trash text");
        post.setTitle("trash title");

        post=indexPostRepository.save(post);

        postService.refreshFulltextIndex();

        Assert.assertFalse(indexPostRepository.findById(post.getId()).isPresent());
    }
}

