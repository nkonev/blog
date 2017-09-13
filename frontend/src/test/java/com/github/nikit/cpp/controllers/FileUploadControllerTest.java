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
import org.springframework.test.web.servlet.MvcResult;
import org.junit.Assert;

public class FileUploadControllerTest extends AbstractUtTestRunner {

	private static final String PUT_IMAGE_URL_TEMPLATE = "/api/post/{id}/title-image";

    @WithUserDetails(TestConstants.USER_ALICE)
    @Test
    public void putImage() throws Exception {
		final long postId = 1;

		byte[] img0 = {(byte)0xFF, (byte)0x01, (byte)0x1A};
        putImage(postId, img0);
		
		byte[] img1 = {(byte)0xAA, (byte)0xBB, (byte)0xCC, (byte)0xDD, (byte)0xCC};
		putImage(postId, img1);
    }
	
	private void putImage(long postId, byte[] bytes) throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.put(PUT_IMAGE_URL_TEMPLATE, postId)
				.header("Content-Length", bytes.length)
                .content(bytes)
				.with(csrf())
        )
                .andExpect(status().isOk());
				
		MvcResult result =mockMvc.perform(
                MockMvcRequestBuilders.get(PUT_IMAGE_URL_TEMPLATE, postId)
        )
                .andExpect(status().isOk())
				.andReturn()
				;
		byte[] content = result.getResponse().getContentAsByteArray();
		
		Assert.assertArrayEquals(bytes, content);
    }
}