package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.AbstractUtTestRunner;
import com.github.nikit.cpp.TestConstants;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.web.servlet.MvcResult;
import org.junit.Assert;

public class ImageUploadControllerTest extends AbstractUtTestRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImageUploadControllerTest.class);

	private static final String PUT_IMAGE_URL_TEMPLATE = com.github.nikit.cpp.controllers.ImageUploadController.POST_TITLE_IMAGE_URL_TEMPLATE;

    @WithUserDetails(TestConstants.USER_NIKITA)
    @Test
    public void putImage() throws Exception {
		final long postId = 1;

		byte[] img0 = {(byte)0xFF, (byte)0x01, (byte)0x1A};
        putImage(postId, img0);
		
		byte[] img1 = {(byte)0xAA, (byte)0xBB, (byte)0xCC, (byte)0xDD, (byte)0xCC};
		putImage(postId, img1);
    }
	
	private void putImage(long postId, byte[] bytes) throws Exception {
		MockMultipartFile imgPart = new MockMultipartFile(ImageUploadController.IMAGE_PART, "lol.png", "image/png", bytes);
		MvcResult mvcResult = mockMvc.perform(
		MockMvcRequestBuilders.fileUpload(PUT_IMAGE_URL_TEMPLATE, postId)
				.file(imgPart).with(csrf())
		)
				.andExpect(status().isOk())
				.andReturn()
		;
		String urlResponse = mvcResult.getResponse().getContentAsString();
		LOGGER.info("responsed image url: {}", urlResponse);

		MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get(urlResponse)
        )
                .andExpect(status().isOk())
				.andReturn()
				;
		byte[] content = result.getResponse().getContentAsByteArray();
		
		Assert.assertArrayEquals(bytes, content);
    }
}