package com.github.nkonev.blog.listener.hibernate;

import com.github.nkonev.blog.converter.PostConverter;
import com.github.nkonev.blog.dto.PostDTO;
import com.github.nkonev.blog.entity.jpa.Post;
import com.github.nkonev.blog.services.SeoCacheService;
import com.github.nkonev.blog.services.WebSocketService;
import org.hibernate.event.spi.*;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import static com.github.nkonev.blog.Constants.CUSTOM_PRERENDER_ENABLE;

@Component
public class BlogInsertListener implements PostInsertEventListener {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(BlogInsertListener.class);

    private static final long serialVersionUID = 6798233539917338414L;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private PostConverter postConverter;

    @Autowired
    private SeoCacheService seoCacheService;

    @Autowired
    private Environment environment;

    @Override
    public void onPostInsert(PostInsertEvent event) {
        LOGGER.trace("object: {}", event.getEntity());
        if (event.getEntity() instanceof Post) {
            Post post = (Post) event.getEntity();
            PostDTO postDTO = postConverter.convertToPostDTOWithCleanTags(post);
            webSocketService.sendInsertPostEvent(postDTO);
            if (environment.getProperty(CUSTOM_PRERENDER_ENABLE, boolean.class, false)) {
                seoCacheService.rewriteCachedPost(post.getId());
                seoCacheService.rewriteCachedIndex();
            }
            LOGGER.debug("sql insert: {}", post);
        }
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        return false;
    }
}
