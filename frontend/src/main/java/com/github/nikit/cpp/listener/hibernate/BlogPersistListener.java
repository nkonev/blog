package com.github.nikit.cpp.listener.hibernate;

import com.github.nikit.cpp.converter.PostConverter;
import com.github.nikit.cpp.dto.PostDTO;
import com.github.nikit.cpp.entity.jpa.Post;
import com.github.nikit.cpp.services.WebSocketService;
import org.hibernate.event.internal.DefaultPersistEventListener;
import org.hibernate.event.internal.DefaultSaveOrUpdateEventListener;
import org.hibernate.event.spi.PersistEvent;
import org.hibernate.event.spi.PersistEventListener;
import org.hibernate.event.spi.SaveOrUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlogPersistListener extends DefaultPersistEventListener {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(BlogPersistListener.class);

    private static final long serialVersionUID = 6798233539917338414L;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private PostConverter postConverter;

    @Override
    public void onPersist(PersistEvent event) {
        LOGGER.trace("object: {}", event.getObject());
        if (event.getObject() instanceof Post) {
            Post post = (Post) event.getObject();
            PostDTO postDTO = postConverter.convertToPostDTOWithCleanTags(post);
            webSocketService.sendPostDto(postDTO);
            LOGGER.debug("notified: {}", event.getObject());
        }
        LOGGER.trace("object: {}", event.getObject());
        super.onPersist(event);
    }
}
