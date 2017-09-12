package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.AbstractUtTestRunner;
import com.github.nikit.cpp.TestConstants;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.Test;
import org.junit.experimental.results.ResultMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.FileSystemUtils;

import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

public class FileUploadControllerTest extends AbstractUtTestRunner {

    @WithUserDetails(TestConstants.USER_ALICE)
    @Test
    public void putImage() throws Exception {
        byte[] bytes = "image bytes байты".getBytes(StandardCharsets.UTF_8);

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/post/{id}/title-image", 1)
                .content(bytes)
				.with(csrf())
        )
                .andExpect(status().isOk());
    }

}