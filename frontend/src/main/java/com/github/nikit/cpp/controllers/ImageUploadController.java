package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.config.CustomConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.sql.ResultSet;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.io.OutputStream;
import org.springframework.util.Assert;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.github.nikit.cpp.dto.UserAccountDetailsDTO;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
public class ImageUploadController {

    @Autowired
    private DataSource dataSource;

    @Value("${custom.image.max.bytes}")
    private long maxBytes;

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageUploadController.class);

    @Autowired
    private CustomConfig customConfig;

    public static final String IMAGE_PART = "image";

    public static final String POST_TITLE_IMAGE_URL_TEMPLATE = "/api/post/{id}";
    public static final String POST_TITLE_IMAGE_URL_TEMPLATE_WITH_FILENAME = POST_TITLE_IMAGE_URL_TEMPLATE + "/{filename}";

	// /post/123/title.png -> /post/123/title.png
	// /post/123/wqwqw.png -> /post/123/1.png
	// /post/123/sasasas.png -> /post/123/2.png
	// /user/123/kitty.png -> /user/123/avatar.png

    @PostMapping(POST_TITLE_IMAGE_URL_TEMPLATE)
	@PreAuthorize("@blogSecurityService.hasImagePermission(#postId, #userAccount, T(com.github.nikit.cpp.entity.jpa.Permissions).EDIT)")
    public String putImage(
            @RequestPart(value = IMAGE_PART) MultipartFile imagePart,
			@PathVariable("id")long postId,
			@NotNull @AuthenticationPrincipal UserAccountDetailsDTO userAccount
	) throws SQLException, IOException {
		long contentLength = getCorrectContentLength(imagePart.getSize());
        String contentType = imagePart.getContentType();
        MediaType.valueOf(contentType);

        try(Connection conn = dataSource.getConnection();) {
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO posts.post_title_image VALUES (?, NULL, NULL) ON CONFLICT(post_id) DO NOTHING");) {
                ps.setLong(1, postId);
                ps.executeUpdate();
            }
            // https://jdbc.postgresql.org/documentation/head/binary-data.html
            try (PreparedStatement ps = conn.prepareStatement("UPDATE posts.post_title_image SET img = ?, content_type = ? WHERE post_id = ?");) {
                ps.setLong(3, postId);
                ps.setString(2, contentType);
                ps.setBinaryStream(1, imagePart.getInputStream(), (int) contentLength);
                ps.executeUpdate();
            }
        }
        return UriComponentsBuilder.fromUriString(customConfig.getBaseUrl() + POST_TITLE_IMAGE_URL_TEMPLATE_WITH_FILENAME)
                .buildAndExpand(postId, "title."+getExtension(imagePart.getOriginalFilename()))
                .toUriString();
    }

    private long getCorrectContentLength(long contentLength) {
        if (contentLength > maxBytes) {
            throw new RuntimeException("Image must be <= "+ maxBytes + " bytes");
        }
        return contentLength;
    }

    private String getExtension(String filename) {
        Assert.notNull(filename, "cannot be null");
        String[] parts = filename.split("\\.");
        return parts[parts.length-1];
    }

	@GetMapping(POST_TITLE_IMAGE_URL_TEMPLATE_WITH_FILENAME)
    public HttpHeaders getImage(
            @PathVariable("id")long postId,
            @PathVariable("filename")String filename,
            OutputStream response) throws SQLException, java.io.IOException {
        try(Connection conn = dataSource.getConnection();) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT img, content_type FROM posts.post_title_image WHERE post_id = ?");) {
                ps.setLong(1, postId);
                try (ResultSet rs = ps.executeQuery();) {
					if(rs.next()) {
						try(InputStream imgStream = rs.getBinaryStream("img");){
							copy(imgStream, response);
						}
						String contentType = rs.getString("content_type");
                        HttpHeaders httpHeaders = new HttpHeaders();
                        httpHeaders.setContentType(MediaType.valueOf(contentType));
                        return httpHeaders;
					} else {
						throw new RuntimeException("Title image not found for post " + postId);
					}
				}
            }
        }
    }
		
	private void copy(InputStream from, OutputStream to) throws java.io.IOException {
		byte[] buffer = new byte[4 * 1024];
		int len;
		while ((len = from.read(buffer)) != -1) {
			to.write(buffer, 0, len);
		}
	}
}
 