package com.github.nkonev.blog.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DbCleaner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbCleaner.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void clean() {
        final int deletedPostContent = clearPostContentImages();
        final int deletedPostTitles  = clearPostTitleImages();
        final int deletedAvatarImages = clearAvatarImages();

        LOGGER.info("Cleared {} post content images; {} post title images; {} user avatar images", deletedPostContent, deletedPostTitles, deletedAvatarImages);
    }

    public int clearPostContentImages() {
        return jdbcTemplate.update("delete from images.post_content_image where id in (select i.id from images.post_content_image i left join posts.post p on p.text like '%' || '/api/image/post/content/' || i.id || '%' where p.id is null);");
    }

    public int clearPostTitleImages(){
        return jdbcTemplate.update("delete from images.post_title_image where id in (select i.id from images.post_title_image i left join posts.post p on p.title_img like '%' || '/api/image/post/title/' || i.id || '%' where p.id is null);");
    }

    public int clearAvatarImages(){
        return jdbcTemplate.update("delete from images.user_avatar_image where id in (select i.id from images.user_avatar_image i left join auth.users u on u.avatar like '%' || '/api/image/user/avatar/' || i.id || '%' where u.id is null);");
    }
}
