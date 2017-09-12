package com.github.nikit.cpp.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
public class FileUploadController {

    @Autowired
    private DataSource dataSource;

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadController.class);

    @PutMapping("/api/post/{id}/title-image")
    @PreAuthorize("isAuthenticated()")
    public void putImage(InputStream is, @RequestHeader HttpHeaders headers, @PathVariable("id")long postId) {
        // https://jdbc.postgresql.org/documentation/head/binary-data.html
        try( Connection conn = dataSource.getConnection();) {
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO posts.post_title_image VALUES (?, ?)");) {
                long contentLength = headers.getContentLength();
                ps.setLong(1, postId);
                ps.setBinaryStream(2, is, (int) contentLength);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            LOGGER.error("SQL Exception on put post {} title", postId, e);
        }
    }
}
