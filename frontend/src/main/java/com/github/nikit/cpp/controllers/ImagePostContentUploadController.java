package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.dto.UserAccountDetailsDTO;
import org.springframework.http.HttpHeaders;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class ImagePostContentUploadController extends AbstractImageUploadController {

    public static final String POST_CONTENT_IMAGE_URL_TEMPLATE = "/api/post/{post_id}/content";
    public static final String POST_CONTENT_IMAGE_URL_TEMPLATE_WITH_FILENAME = POST_CONTENT_IMAGE_URL_TEMPLATE + "/{id}.{ext}";

    @PostMapping(POST_CONTENT_IMAGE_URL_TEMPLATE)
    @PreAuthorize("@blogSecurityService.hasPostContentImagePermission(#postId, #userAccount, T(com.github.nikit.cpp.entity.jpa.Permissions).EDIT)")
    public String putImage(
            @RequestPart(value = IMAGE_PART) MultipartFile imagePart,
            @PathVariable("post_id")long postId,
            @NotNull @AuthenticationPrincipal UserAccountDetailsDTO userAccount
    ) throws SQLException, IOException {
        Map<String, Object> map = new HashMap<>();
        map.put(POST_ID, postId);
        return super.putImage(imagePart, map, userAccount);
    }

    private static final String ID = "id";
    private static final String POST_ID = "post_id";

    private Map<String, Object> toComplexId(long id, long postId) {
        Map<String, Object> map = new HashMap<>();
        map.put(ID, id);
        map.put(POST_ID, postId);
        return map;
    }

    private long toLongId(Map<String, Object> id) {
        return (long) id.get(ID);
    }

    private long toLongPostId(Map<String, Object> id) {
        return (long) id.get(POST_ID);
    }

    @Override
    protected PreparedStatement buildNoConflictInsertPreparedStatement(Connection conn, Map<String, Object> id) throws SQLException {
        // only this SQL returns id and post_id always. If ON CONFLICT present it can not return ids
        PreparedStatement ps = conn.prepareStatement("INSERT INTO images.post_content_image(post_id, id, img, content_type) VALUES (?, DEFAULT, NULL, NULL) RETURNING id, post_id;");
        ps.setLong(1, toLongPostId(id));
        return ps;
    }

    @Override
    protected Map<String, Object> getIdentificators(ResultSet identificators) throws SQLException {
        long id = identificators.getLong("id");
        long postId = identificators.getLong("post_id");
        return toComplexId(id, postId);
    }


    @Override
    protected PreparedStatement buildUpdatePreparedStatement(Connection conn, Map<String, Object> id, Optional<Map<String, Object>> intermediateIdentificators, String contentType, InputStream is, long contentLength) throws SQLException {
        Map<String, Object> ii = intermediateIdentificators.orElseThrow(()->new RuntimeException("Intermediate identificators didn't been provided"));
        PreparedStatement ps = conn.prepareStatement("UPDATE images.post_content_image SET img = ?, content_type = ? WHERE id = ? and post_id = ?");

        ps.setLong(4, toLongPostId(ii));
        ps.setLong(3, toLongId(ii));
        ps.setString(2, contentType);
        ps.setBinaryStream(1, is, (int) contentLength);
        return ps;
    }

    @Override
    protected String getUrl(Map<String, Object> id, Optional<Map<String, Object>> intermediateIdentificators, String originFilename) {
        Map<String, Object> ii = intermediateIdentificators.orElseThrow(()->new RuntimeException("Intermediate identificators didn't been provided"));
        return UriComponentsBuilder.fromUriString(customConfig.getBaseUrl() + POST_CONTENT_IMAGE_URL_TEMPLATE_WITH_FILENAME)
                .buildAndExpand(toLongPostId(ii), toLongId(ii), getExtension(originFilename))
                .toUriString();
    }

    ///////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////

    @GetMapping(POST_CONTENT_IMAGE_URL_TEMPLATE_WITH_FILENAME)
    public HttpHeaders getImage(
            @PathVariable("post_id")long postId,
            @PathVariable("id")long id,
            OutputStream response
    ) throws SQLException, IOException {
        return super.getImage(toComplexId(id, postId), response);
    }

    @Override
    protected PreparedStatement buildSelectImage(Connection conn, Map<String, Object> id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT img, content_type FROM images.post_content_image WHERE id = ? AND post_id = ?");
        ps.setLong(2, toLongPostId(id));
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
 