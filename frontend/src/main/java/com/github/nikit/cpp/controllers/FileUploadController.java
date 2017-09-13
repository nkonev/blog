package com.github.nikit.cpp.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.springframework.web.bind.annotation.RequestHeader;
import java.io.OutputStream;

@RestController
public class FileUploadController {

    @Autowired
    private DataSource dataSource;

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadController.class);

    @PutMapping("/api/post/{id}/title-image")
    @PreAuthorize("isAuthenticated()")
    public void putImage(InputStream is, @RequestHeader HttpHeaders headers, @PathVariable("id")long postId) throws SQLException {
        // https://jdbc.postgresql.org/documentation/head/binary-data.html
		// TODO check content-length
		// TODO UPSERT
		// TODO delete
		// TODO store not only for image title
        try( Connection conn = dataSource.getConnection();) {
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO posts.post_title_image VALUES (?, ?)");) {
                long contentLength = headers.getContentLength();
                ps.setLong(1, postId);
                ps.setBinaryStream(2, is, (int) contentLength);
                ps.executeUpdate();
            }
        } 
    }
	
	@GetMapping("/api/post/{id}/title-image")
    public void getImage(@PathVariable("id")long postId, OutputStream response) throws SQLException, java.io.IOException {
        try( Connection conn = dataSource.getConnection();) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT img FROM posts.post_title_image WHERE post_id = ?");) {
                ps.setLong(1, postId);
                try (ResultSet rs = ps.executeQuery();) {
					if(rs.next()) {
						try(InputStream imgStream = rs.getBinaryStream("img");){
							copy(imgStream, response);
						}
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
 