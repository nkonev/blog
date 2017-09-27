package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.dto.UserAccountDetailsDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
public class ImagePostTitleUploadController extends AbstractImageUploadController {

    public static final String POST_TITLE_IMAGE_URL_TEMPLATE = "/api/post/{post_id}";
    public static final String POST_TITLE_IMAGE_URL_TEMPLATE_WITH_FILENAME = POST_TITLE_IMAGE_URL_TEMPLATE + "/{filename}";

    @PostMapping(POST_TITLE_IMAGE_URL_TEMPLATE)
    @PreAuthorize("@blogSecurityService.hasPostTitleImagePermission(#postId, #userAccount, T(com.github.nikit.cpp.entity.jpa.Permissions).EDIT)")
    public String putImage(
            @RequestPart(value = IMAGE_PART) MultipartFile imagePart,
            @PathVariable("post_id")long postId,
            @NotNull @AuthenticationPrincipal UserAccountDetailsDTO userAccount
    ) throws SQLException, IOException {
        return super.putImage(imagePart, toComplexId(postId), userAccount);
    }

    private static final String POST_ID = "post_id";

    private Map<String, Object> toComplexId(long postId) {
        return Collections.singletonMap(POST_ID, postId);
    }

    private long toLongId(Map<String, Object> id) {
        return (long) id.get(POST_ID);
    }

    @Override
    protected PreparedStatement buildNoConflictInsertPreparedStatement(Connection conn, Map<String, Object> id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO images.post_title_image(post_id, img, content_type) VALUES (?, NULL, NULL) ON CONFLICT(post_id) DO NOTHING RETURNING 0");
        ps.setLong(1, toLongId(id));
        return ps;
    }

    @Override
    protected PreparedStatement buildUpdatePreparedStatement(Connection conn, Map<String, Object> id, Optional<Map<String, Object>> intermediateIdentificators, String contentType, InputStream is, long contentLength) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("UPDATE images.post_title_image SET img = ?, content_type = ? WHERE post_id = ?");
        ps.setLong(3, toLongId(id));
        ps.setString(2, contentType);
        ps.setBinaryStream(1, is, (int) contentLength);
        return ps;
    }

    private String generateFileName(String originFileName) {
        return "title."+getExtension(originFileName);
    }

    @Override
    protected String getUrl(Map<String, Object> id, Optional<Map<String, Object>> intermediateIdentificators, String originFilename) {
        return UriComponentsBuilder.fromUriString(customConfig.getBaseUrl() + POST_TITLE_IMAGE_URL_TEMPLATE_WITH_FILENAME)
                .buildAndExpand(toLongId(id), generateFileName(originFilename))
                .toUriString();
    }

    ///////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////

    @GetMapping(POST_TITLE_IMAGE_URL_TEMPLATE_WITH_FILENAME)
    public HttpHeaders getImage(
            @PathVariable("post_id")long postId,
            @PathVariable("filename")String filename,
            OutputStream response) throws SQLException, IOException {
        return super.getImage(toComplexId(postId), response);
    }

    @Override
    protected PreparedStatement buildSelectImage(Connection conn, Map<String, Object> id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT img, content_type FROM images.post_title_image WHERE post_id = ?");
        ps.setLong(1, toLongId(id));
        return ps;
    }

    @Override
    protected void copyToOutputStream(ResultSet rs, OutputStream response) throws SQLException, IOException {
        try(InputStream imgStream = rs.getBinaryStream("img");){
            copy(imgStream, response);
        }
    }

    @Override
    protected String getContentType(ResultSet rs) throws SQLException {
        return rs.getString("content_type");
    }
}
 