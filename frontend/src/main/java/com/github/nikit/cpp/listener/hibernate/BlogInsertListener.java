package com.github.nikit.cpp.listener.hibernate;

import com.github.nikit.cpp.converter.PostConverter;
import com.github.nikit.cpp.dto.PostDTO;
import com.github.nikit.cpp.entity.jpa.Post;
import com.github.nikit.cpp.services.WebSocketService;
import org.hibernate.event.spi.*;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlogInsertListener implements PostInsertEventListener {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(BlogInsertListener.class);

    private static final long serialVersionUID = 6798233539917338414L;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private PostConverter postConverter;

    @Override
    public void onPostInsert(PostInsertEvent event) {
        LOGGER.trace("object: {}", event.getEntity());
        if (event.getEntity() instanceof Post) {
            Post post = (Post) event.getEntity();
            PostDTO postDTO = postConverter.convertToPostDTOWithCleanTags(post);
            webSocketService.sendPostDto(postDTO);
            LOGGER.debug("sql insert: {}", post);
        }
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        return false;
    }
}
