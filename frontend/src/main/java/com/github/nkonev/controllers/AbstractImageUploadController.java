package com.github.nkonev.controllers;

import com.github.nkonev.config.CustomConfig;
import com.github.nkonev.config.ImageConfig;
import com.github.nkonev.exception.PayloadTooLargeException;
import com.github.nkonev.exception.UnsupportedMessageTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AbstractImageUploadController {

    @Autowired
    private DataSource dataSource;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractImageUploadController.class);

    @Autowired
    protected CustomConfig customConfig;

    @Autowired
    protected ImageConfig imageConfig;

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

    protected ImageResponse postImage(
            MultipartFile imagePart,
            UpdateImage updateImage,
            Function<UUID, ImageResponse> produceUrl
	) throws SQLException, IOException {
		long contentLength = getCorrectContentLength(imagePart.getSize());
        String contentType = getCorrectContentType(imagePart.getContentType());

        try(Connection conn = dataSource.getConnection();) {
            return produceUrl.apply(updateImage.updateImage(conn, contentLength, contentType));
        }
    }

    private String getCorrectContentType(String contentType) {
        MediaType inputMt = MediaType.valueOf(contentType);

        for(MediaType mediaType: imageConfig.getAllowedMimeTypes()){
            if (mediaType.isCompatibleWith(inputMt)) {
                return contentType;
            }
        }
        throw new UnsupportedMessageTypeException("Incompatible content type. Allowed: " + imageConfig.getAllowedMimeTypes());
    }

    private long getCorrectContentLength(long contentLength) {
        if (contentLength > imageConfig.getMaxBytes()) {
            throw new PayloadTooLargeException("Image must be <= "+ imageConfig.getMaxBytes() + " bytes");
        }
        return contentLength;
    }

    protected String getExtension(String contentType) {
        Assert.notNull(contentType, "cannot be null");
        MediaType mt = MediaType.valueOf(contentType);
        return mt.getSubtype();
    }

    protected void getImage(
            Consumer<Connection> buildResponse
    ) throws SQLException, IOException {
        try(Connection conn = dataSource.getConnection();) {
            buildResponse.accept(conn);
        }
    }

    protected void copyStream(InputStream from, OutputStream to) throws IOException {
		byte[] buffer = new byte[4 * 1024];
		int len;
		while ((len = from.read(buffer)) != -1) {
			to.write(buffer, 0, len);
		}
	}
}
 