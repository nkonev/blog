package com.github.nkonev.blog.converter;

import com.github.nkonev.blog.AbstractUtTestRunner;
import com.github.nkonev.blog.dto.PostDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.StringUtils;

import java.util.Map;

public class PostConverterTest extends AbstractUtTestRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostConverterTest.class);

    @Autowired
    private PostConverter postConverter;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    public void testGetYouTubeId() {
        String youtubeVideoId = PostConverter.getYouTubeVideoId("https://www.youtube.com/embed/eoDsxos6xhM?showinfo=0");
        Assertions.assertEquals("eoDsxos6xhM", youtubeVideoId);
    }

    @Test
    public void shouldSetFirstImgWhenTitleImgEmpty() {
        PostDTO postDTO = new PostDTO();
        postDTO.setText("Hello, I contains images. This is first. <img src=\"http://example.com/image1.png\"/> This is second. <img src=\"http://example.com/image2.png\"/>");
        postDTO.setTitle("Title");
        String titleImg = postConverter.getTitleImg(postDTO);
        Assertions.assertEquals("http://example.com/image1.png", titleImg);
    }

    @Test
    public void shouldDownloadYoutubePreviewWhenTitleImgEmptyAndContentHasNotImages() {
        PostDTO postDTO = new PostDTO();
        postDTO.setText("Hello, I contains youtube videos. " +
            " This is first. <iframe allowfullscreen=\"true\" src=\"https://www.youtube.com/embed/ava?showinfo=0\" frameborder=\"0\"></iframe>"
            +
            " This is second. <iframe allowfullscreen=\"true\" src=\"https://www.youtube.com/embed/ava?showinfo=0\" frameborder=\"0\"></iframe>");
        postDTO.setTitle("Title");
        String titleImg = postConverter.getTitleImg(postDTO);
        Assertions.assertNotNull(titleImg);
        String imageId = titleImg
            .replace("/api/image/post/title/", "")
            .replace(".png", "");
        Assertions.assertFalse(StringUtils.isEmpty(imageId));

        Integer integer = jdbcTemplate.queryForObject(
            "select count(*) from images.post_title_image where id = :id\\:\\:uuid",
            Map.of("id", imageId), Integer.class);
        Assertions.assertEquals(1, integer);
    }
}
