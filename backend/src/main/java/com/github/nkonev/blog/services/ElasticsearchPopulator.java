package com.github.nkonev.blog.services;

import com.github.nkonev.blog.converter.PostConverter;
import com.github.nkonev.blog.entity.elasticsearch.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Optional;

@Service
public class ElasticsearchPopulator {
    @Autowired
    private ElasticsearchOperations es;

    @Autowired
    private com.github.nkonev.blog.repo.elasticsearch.IndexPostRepository indexPostRepositoryElastic;

    @Autowired
    private com.github.nkonev.blog.repo.jpa.PostRepository postRepositoryJpa;

    @Autowired
    private PostConverter postConverter;

    @Value("${custom.elasticsearch.drop-first:true}")
    private boolean dropFirst;

    @Value("${custom.elasticsearch.refresh-on-start:true}")
    private boolean refresh;

    private static final Logger LOGGER  = LoggerFactory.getLogger(ElasticsearchPopulator.class);

    @PostConstruct
    public void pc(){

        if (dropFirst) {
            try {
                LOGGER.info("Droppping elasticsearch index");
                es.deleteIndex(Post.INDEX);
            } catch (Exception e) {
                LOGGER.error("Error during elasticsearch index");
            }
        }

        if (refresh) {
            LOGGER.info("Starting refreshing elasticsearch index");
            final Collection<Long> postIds = postRepositoryJpa.findPostIds();
            for (Long id : postIds) {

                Optional<com.github.nkonev.blog.entity.jpa.Post> post = postRepositoryJpa.findById(id);
                if (post.isPresent()) {
                    com.github.nkonev.blog.entity.jpa.Post jpaPost = post.get();
                    LOGGER.info("Copying post: {}", id);
                    indexPostRepositoryElastic.save(postConverter.toElasticsearchPost(jpaPost));
                }
            }
            LOGGER.info("Finished refreshing elasticsearch index");
        }
    }

}
