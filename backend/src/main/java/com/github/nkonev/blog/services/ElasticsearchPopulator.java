package com.github.nkonev.blog.services;

import com.github.nkonev.blog.converter.PostConverter;
import com.github.nkonev.blog.entity.elasticsearch.Post;
import com.github.nkonev.blog.utils.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Optional;

@Service
public class ElasticsearchPopulator {

    @Autowired
    private com.github.nkonev.blog.repo.elasticsearch.IndexPostRepository indexPostRepositoryElastic;

    @Autowired
    private com.github.nkonev.blog.repo.jpa.PostRepository postRepositoryJpa;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private PostConverter postConverter;

    @Value("${custom.elasticsearch.drop-first:true}")
    private boolean dropFirst;

    @Value("${custom.elasticsearch.refresh-on-start:true}")
    private boolean refresh;

    @Value("${custom.elasticsearch.create-index:true}")
    private boolean createIndex;

    @Value("classpath:/config/index-post.json")
    private Resource indexSettings;

    @Value("classpath:/config/index-post-mapping.json")
    private Resource postMapping;

    private static final Logger LOGGER  = LoggerFactory.getLogger(ElasticsearchPopulator.class);

    @PostConstruct
    public void pc(){

        if (dropFirst) {
            try {
                LOGGER.info("Dropping elasticsearch index");
                elasticsearchTemplate.deleteIndex(Post.INDEX);
            } catch (Exception e) {
                LOGGER.error("Error during dropping elasticsearch index");
            }
        }

        if (createIndex) {
            try {
                LOGGER.info("Creating elasticsearch index");
                final String settings = ResourceUtils.stringFromResource(indexSettings);
                elasticsearchTemplate.createIndex(Post.INDEX, settings);

                final String mapping = ResourceUtils.stringFromResource(postMapping);
                elasticsearchTemplate.putMapping(Post.INDEX, Post.TYPE, mapping);
                LOGGER.info("Successfully created elasticsearch index");
            } catch (Exception e) {
                LOGGER.error("Error during create elasticsearch index", e);
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
                    indexPostRepositoryElastic.save(PostConverter.toElasticsearchPost(jpaPost));
                }
            }
            LOGGER.info("Finished refreshing elasticsearch index");
        }
    }

}
