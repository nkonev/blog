package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.config.CustomConfig;
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
import java.util.UUID;
import java.util.function.*;

public abstract class AbstractImageUploadController {

    @Autowired
    private DataSource dataSource;

    @Value("${custom.image.max.bytes}")
    private long maxBytes;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractImageUploadController.class);

    @Autowired
    protected CustomConfig customConfig;

    public static final String IMAGE_PART = "image";

    @FunctionalInterface
    public interface UpdateImage {
        UUID updateImage(Connection conn, long contentLength, String contentType);
    }

    public static class ImageResponse {
        private String relativeUrl;
        private String url;

        public ImageResponse() { }

        public ImageResponse(String relativeUrl, String url) {
            this.relativeUrl = relativeUrl;
            this.url = url;
        }

        public String getRelativeUrl() {
            return relativeUrl;
        }

        public void setRelativeUrl(String relativeUrl) {
            this.relativeUrl = relativeUrl;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public ImageResponse putImage(
            MultipartFile imagePart,
            UpdateImage updateImage,
            Function<UUID, ImageResponse> produceUrl
	) throws SQLException, IOException {
		long contentLength = getCorrectContentLength(imagePart.getSize());
        String contentType = imagePart.getContentType();
        MediaType.valueOf(contentType);

        try(Connection conn = dataSource.getConnection();) {
            return produceUrl.apply(updateImage.updateImage(conn, contentLength, contentType));
        }
    }

    private long getCorrectContentLength(long contentLength) {
        if (contentLength > maxBytes) {
            throw new RuntimeException("Image must be <= "+ maxBytes + " bytes");
        }
        return contentLength;
    }

    protected String getExtension(String contentType) {
        Assert.notNull(contentType, "cannot be null");
        MediaType mt = MediaType.valueOf(contentType);
        return mt.getSubtype();
    }

    public HttpHeaders getImage(
            Function<Connection, HttpHeaders> buildResponse
    ) throws SQLException, IOException {
        try(Connection conn = dataSource.getConnection();) {
            return buildResponse.apply(conn);
        }
    }

    protected HttpHeaders buildHeaders(String contentType) throws SQLException {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.valueOf(contentType));
        return httpHeaders;
    }

    protected void copyStream(InputStream from, OutputStream to) throws IOException {
		byte[] buffer = new byte[4 * 1024];
		int len;
		while ((len = from.read(buffer)) != -1) {
			to.write(buffer, 0, len);
		}
	}
}
 