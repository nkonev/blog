package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.AbstractUtTestRunner;
import com.github.nikit.cpp.TestConstants;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ImagePostContentUploadControllerTest extends AbstractUtTestRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImagePostContentUploadControllerTest.class);

	private static final String PUT_IMAGE_URL_TEMPLATE = ImagePostContentUploadController.POST_CONTENT_IMAGE_URL_TEMPLATE;
	private static final String POST_CONTENT_IMAGE_URL_TEMPLATE_WITH_FILENAME = ImagePostContentUploadController.POST_CONTENT_IMAGE_URL_TEMPLATE_WITH_FILENAME;


	@WithUserDetails(TestConstants.USER_NIKITA)
    @Test
    public void putImage() throws Exception {
		final long postId = 1;

		byte[] img0 = {(byte)0xFF, (byte)0x01, (byte)0x1A};
        String url0 = putImage(postId, img0);
		
		byte[] img1 = {(byte)0xAA, (byte)0xBB, (byte)0xCC, (byte)0xDD, (byte)0xCC};
		putImage(postId, img1);

		// check that first image didn't changed
		byte[] content0 = getImage(url0);
		Assert.assertArrayEquals(img0, content0);
    }

	@Test
	public void getUnexistingImage() throws Exception {
		final long notExistsPostContentImageId = 100_000_000;

		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get(POST_CONTENT_IMAGE_URL_TEMPLATE_WITH_FILENAME, 1, notExistsPostContentImageId, "png")
		)
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("data not found"))
				.andExpect(jsonPath("$.message").value("post content image with id '100000000' not found"))
				.andReturn()
				;
	}


	private String putImage(long postId, byte[] bytes) throws Exception {
		MockMultipartFile imgPart = new MockMultipartFile(ImagePostTitleUploadController.IMAGE_PART, "lol-content.png", "image/png", bytes);
		MvcResult mvcResult = mockMvc.perform(
		MockMvcRequestBuilders.fileUpload(PUT_IMAGE_URL_TEMPLATE, postId)
				.file(imgPart).with(csrf())
		)
				.andExpect(status().isOk())
				.andReturn()
		;
		String urlResponse = mvcResult.getResponse().getContentAsString();
		LOGGER.info("responsed image url: {}", urlResponse);

		byte[] content = getImage(urlResponse);
		
		Assert.assertArrayEquals(bytes, content);

		return urlResponse;
    }

    private byte[] getImage(String url) throws Exception {
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get(url)
		)
				.andExpect(status().isOk())
				.andReturn()
				;
		return result.getResponse().getContentAsByteArray();
	}
}