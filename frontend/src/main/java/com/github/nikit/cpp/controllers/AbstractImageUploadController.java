package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.config.CustomConfig;
import com.github.nikit.cpp.dto.UserAccountDetailsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractImageUploadController {

    @Autowired
    private DataSource dataSource;

    @Value("${custom.image.max.bytes}")
    private long maxBytes;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractImageUploadController.class);

    @Autowired
    protected CustomConfig customConfig;

    public static final String IMAGE_PART = "image";


	// /post/123/title.png -> /post/123/title.png
	// /post/123/content/wqwqw.png -> /post/123/content/_timestamp_.png
	// /post/123/content/sasasas.png -> /post/123/content/_timestamp_.png
	// /user/123/kitty.png -> /user/123/avatar.png

    public String putImage(
            MultipartFile imagePart,
            Map<String, Object> id,
			UserAccountDetailsDTO userAccount
	) throws SQLException, IOException {
		long contentLength = getCorrectContentLength(imagePart.getSize());
        String contentType = imagePart.getContentType();
        MediaType.valueOf(contentType);

        Optional<Map<String, Object>> intermediateIdentificators = Optional.empty();
        try(Connection conn = dataSource.getConnection();) {
            try (PreparedStatement ps = buildNoConflictInsertPreparedStatement(conn, id)) {
                try (ResultSet identificators = ps.executeQuery();) {
                    if (identificators.next()) {
                        intermediateIdentificators = Optional.ofNullable(getIdentificators(identificators));
                    }
                }
            }
            // https://jdbc.postgresql.org/documentation/head/binary-data.html
            try (PreparedStatement ps = buildUpdatePreparedStatement(conn, id, intermediateIdentificators, contentType, imagePart.getInputStream(), contentLength)) {
                ps.executeUpdate();
            }
        }
        return getUrl(id, intermediateIdentificators, imagePart.getOriginalFilename());
    }

    protected Map<String, Object> getIdentificators(ResultSet identificators) throws SQLException {
        return null;
    }

    protected abstract String getUrl(Map<String, Object> id, Optional<Map<String, Object>> intermediateIdentificators, String originFilename);

    protected abstract PreparedStatement buildNoConflictInsertPreparedStatement(Connection conn, Map<String, Object> id) throws SQLException;

    protected abstract PreparedStatement buildUpdatePreparedStatement(Connection conn, Map<String, Object> id, Optional<Map<String, Object>> intermediateIdentificators, String contentType, InputStream is, long contentLength) throws SQLException;

    private long getCorrectContentLength(long contentLength) {
        if (contentLength > maxBytes) {
            throw new RuntimeException("Image must be <= "+ maxBytes + " bytes");
        }
        return contentLength;
    }

    protected String getExtension(String filename) {
        Assert.notNull(filename, "cannot be null");
        String[] parts = filename.split("\\.");
        return parts[parts.length-1];
    }

    public HttpHeaders getImage(
            Map<String, Object> id,
            OutputStream response
    ) throws SQLException, IOException {
        try(Connection conn = dataSource.getConnection();) {
            try (PreparedStatement ps = buildSelectImage(conn, id);) {
                try (ResultSet rs = ps.executeQuery();) {
					if (rs.next()) {
                        copyToOutputStream(rs, response);
						String contentType = getContentType(rs);
                        HttpHeaders httpHeaders = new HttpHeaders();
                        httpHeaders.setContentType(MediaType.valueOf(contentType));
                        return httpHeaders;
					} else {
						throw new RuntimeException("image not found with id" + id);
					}
				}
            }
        }
    }

    protected abstract void copyToOutputStream(ResultSet rs, OutputStream response) throws SQLException, IOException;

    protected abstract PreparedStatement buildSelectImage(Connection conn, Map<String, Object> id) throws SQLException;

    protected abstract String getContentType(ResultSet rs) throws SQLException;

    protected void copy(InputStream from, OutputStream to) throws IOException {
		byte[] buffer = new byte[4 * 1024];
		int len;
		while ((len = from.read(buffer)) != -1) {
			to.write(buffer, 0, len);
		}
	}
}
 