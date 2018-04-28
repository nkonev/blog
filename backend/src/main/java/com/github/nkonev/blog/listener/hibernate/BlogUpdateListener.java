package com.github.nkonev.blog.listener.hibernate;

import com.github.nkonev.blog.converter.PostConverter;
import com.github.nkonev.blog.dto.PostDTO;
import com.github.nkonev.blog.entity.jpa.Post;
import com.github.nkonev.blog.services.SeoCacheService;
import com.github.nkonev.blog.services.WebSocketService;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlogUpdateListener implements PostUpdateEventListener {
    private static final long serialVersionUID = 3341150678491703105L;

    private static transient final Logger LOGGER = LoggerFactory.getLogger(BlogUpdateListener.class);

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private PostConverter postConverter;

    @Autowired
    private SeoCacheService seoCacheService;

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        if (event.getEntity() instanceof Post) {
            Post post = (Post) event.getEntity();
            PostDTO postDTO = postConverter.convertToPostDTOWithCleanTags(post);
            webSocketService.sendUpdatePostEvent(postDTO);
            seoCacheService.rewriteCachedPost(post.getId());
            seoCacheService.rewriteCachedIndex();
            LOGGER.debug("sql update: {}", post);
        }
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        return false;
    }
}
