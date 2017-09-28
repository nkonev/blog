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

@RestController
public class ImageUserAvatarUploadController extends AbstractImageUploadController {

    public static final String AVATAR_IMAGE_URL_TEMPLATE = "/api/user/avatar/{avatar_id}";
    public static final String AVATAR_IMAGE_URL_TEMPLATE_WITH_FILENAME = AVATAR_IMAGE_URL_TEMPLATE + "/{filename}";

    @PostMapping(AVATAR_IMAGE_URL_TEMPLATE)
    @PreAuthorize("@blogSecurityService.hasUserAvatarImagePermission(#avatarId, #userAccount, T(com.github.nikit.cpp.entity.jpa.Permissions).EDIT)")
    public String putImage(
            @RequestPart(value = IMAGE_PART) MultipartFile imagePart,
            @PathVariable("avatar_id")long avatarId,
            @NotNull @AuthenticationPrincipal UserAccountDetailsDTO userAccount
    ) throws SQLException, IOException {
        return super.putImage(
            imagePart,
            (Connection conn) -> {
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO images.user_avatar_image(user_id, img, content_type) VALUES (?, NULL, NULL) ON CONFLICT(user_id) DO NOTHING")){
                    ps.setLong(1, avatarId);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            },
            (conn, contentLength, contentType) -> {
                try (PreparedStatement ps = conn.prepareStatement("UPDATE images.user_avatar_image SET img = ?, content_type = ? WHERE user_id = ?")){
                    ps.setLong(3, avatarId);
                    ps.setString(2, contentType);
                    ps.setBinaryStream(1, imagePart.getInputStream(), (int) contentLength);
                    ps.executeUpdate();
                } catch (SQLException | IOException e) {
                    throw new RuntimeException(e);
                }
            },
            () -> UriComponentsBuilder.fromUriString(customConfig.getBaseUrl() + AVATAR_IMAGE_URL_TEMPLATE_WITH_FILENAME)
                    .buildAndExpand(avatarId, generateFileName(imagePart.getOriginalFilename()))
                    .toUriString()
        );
    }

    private String generateFileName(String originFileName) {
        return "avatar."+getExtension(originFileName);
    }

    ///////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////

    @GetMapping(AVATAR_IMAGE_URL_TEMPLATE_WITH_FILENAME)
    public HttpHeaders getImage(
            @PathVariable("avatar_id")long avatarId,
            @PathVariable("filename")String filename,
            OutputStream response
    ) throws SQLException, IOException {
        return super.getImage(
            (Connection conn) -> {
                try (PreparedStatement ps = conn.prepareStatement("SELECT img, content_type FROM images.user_avatar_image WHERE user_id = ?");) {
                    ps.setLong(1, avatarId);
                    try (ResultSet rs = ps.executeQuery();) {
                        if (rs.next()) {
                            try(InputStream imgStream = rs.getBinaryStream("img");){
                                copyStream(imgStream, response);
                            } catch (SQLException | IOException e) {
                                throw new RuntimeException(e);
                            }
                            return buildHeaders(rs);
                        } else {
                            throw new RuntimeException("avatar image with id '"+avatarId+"' not found");
                        }
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        );
    }

    @Override
    protected String getContentType(ResultSet rs) throws SQLException {
        return rs.getString("content_type");
    }
}
 