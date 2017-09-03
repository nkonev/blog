package com.github.nikit.cpp.listener.hibernate;

import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.stereotype.Component;

@Component
public class BlogUpdateListener implements PostUpdateEventListener {
    private static final long serialVersionUID = 3341150678491703105L;

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        System.out.println();
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        return false;
    }
}
