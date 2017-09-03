package com.github.nikit.cpp.listener.hibernate;

import com.github.nikit.cpp.dto.PostDTO;
import com.github.nikit.cpp.entity.jpa.Post;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BlogUpdateListener implements PostUpdateEventListener {
    private static final long serialVersionUID = 3341150678491703105L;

    private static transient final Logger LOGGER = LoggerFactory.getLogger(BlogUpdateListener.class);

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        if (event.getEntity() instanceof Post) {
            Post post = (Post) event.getEntity();
            LOGGER.debug("sql update: {}", post);
        }
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        return false;
    }
}
