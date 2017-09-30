package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.dto.UserAccountDetailsDTO;
import com.github.nikit.cpp.exception.DataNotFoundException;
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
import java.util.UUID;

@RestController
public class ImageUserAvatarUploadController extends AbstractImageUploadController {

    public static final String PUT_TEMPLATE = "/api/image/user/avatar";
    public static final String GET_TEMPLATE = PUT_TEMPLATE + "/{id}.{ext}";

    @PostMapping(PUT_TEMPLATE)
    @PreAuthorize("isAuthenticated()")
    public ImageResponse putImage(
            @RequestPart(value = IMAGE_PART) MultipartFile imagePart,
            @NotNull @AuthenticationPrincipal UserAccountDetailsDTO userAccount
    ) throws SQLException, IOException {
        return super.putImage(
            imagePart,
            (conn, contentLength, contentType) -> {
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO images.user_avatar_image(img, content_type) VALUES (?, ?) RETURNING id")){
                    ps.setString(2, contentType);
                    ps.setBinaryStream(1, imagePart.getInputStream(), (int) contentLength);
                    try(ResultSet resp = ps.executeQuery()) {
                        if(!resp.next()) {
                            throw new RuntimeException("Expected result");
                        }
                        return resp.getObject("id", UUID.class);
                    }

                } catch (SQLException | IOException e) {
                    throw new RuntimeException(e);
                }
            },
            (uuid) -> {
                String relativeUrl = UriComponentsBuilder.fromUriString(GET_TEMPLATE)
                        .buildAndExpand(uuid, getExtension(imagePart.getContentType()))
                        .toUriString();
                return new ImageResponse(relativeUrl, customConfig.getBaseUrl() + relativeUrl);
            }
        );
    }

    ///////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////

    @GetMapping(GET_TEMPLATE)
    public HttpHeaders getImage(
            @PathVariable("id")UUID id,
            OutputStream response
    ) throws SQLException, IOException {
        return super.getImage(
            (Connection conn) -> {
                try (PreparedStatement ps = conn.prepareStatement("SELECT img, content_type FROM images.user_avatar_image WHERE id = ?");) {
                    ps.setObject(1, id);
                    try (ResultSet rs = ps.executeQuery();) {
                        if (rs.next()) {
                            try(InputStream imgStream = rs.getBinaryStream("img");){
                                copyStream(imgStream, response);
                            } catch (SQLException | IOException e) {
                                throw new RuntimeException(e);
                            }
                            return buildHeaders(rs.getString("content_type"));
                        } else {
                            throw new DataNotFoundException("avatar image with id '"+id+"' not found");
                        }
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        );
    }

}
