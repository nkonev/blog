package com.github.nkonev.blog.controllers;

import com.github.nkonev.blog.dto.UserAccountDetailsDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.sql.SQLException;
import java.util.UUID;

@RestController
public class ImagePostContentUploadController extends AbstractImageUploadController {

    public static final String POST_TEMPLATE = "/api/image/post/content";
    public static final String GET_TEMPLATE = POST_TEMPLATE + "/{id}.{ext}";

    public static final String imageType = "postContentImages";

    @PostMapping(POST_TEMPLATE)
    @PreAuthorize("isAuthenticated()")
    public ImageResponse postImage(
            @RequestPart(value = IMAGE_PART) MultipartFile imagePart,
            @NotNull @AuthenticationPrincipal UserAccountDetailsDTO userAccount
    ) throws SQLException {
        return super.postImage(
            "INSERT INTO images.post_content_image(img, content_type) VALUES (?, ?) RETURNING id;",
            GET_TEMPLATE,
            imagePart
        );
    }


    ///////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////

    @GetMapping(GET_TEMPLATE)
    public void getImage(
            @PathVariable("id")UUID id,
            HttpServletResponse response,
            HttpServletRequest request
    ) {
        super.getImage(
                "SELECT img, length(img) as content_length, content_type, create_date_time FROM images.post_content_image WHERE id = ?",
                id,
                request,
                response,
                imageType,
                "post content image with id '" + id + "' not found"
        );
    }

    public int clearPostContentImages() {
        return jdbcTemplate.update(
                "delete from images.post_content_image where id in (" +
                        "select i.id from images.post_content_image i " +
                        "left join posts.post p on p.text like '%' || '/api/image/post/content/' || i.id || '%' " +
                        "where p.id is null and (i.create_date_time + interval '1 day') < localtimestamp" +
                ");");
    }
}
