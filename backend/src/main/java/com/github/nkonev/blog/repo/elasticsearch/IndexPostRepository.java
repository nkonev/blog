package com.github.nkonev.blog.repo.elasticsearch;

import com.github.nkonev.blog.entity.elasticsearch.Post;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndexPostRepository extends ElasticsearchRepository<Post, Long> {
}
