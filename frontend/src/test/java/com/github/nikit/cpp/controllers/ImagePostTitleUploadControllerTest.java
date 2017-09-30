package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.AbstractUtTestRunner;
import com.github.nikit.cpp.TestConstants;
import com.google.common.net.HttpHeaders;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.web.servlet.MvcResult;
import org.junit.Assert;

public class ImagePostTitleUploadControllerTest extends AbstractUtTestRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImagePostTitleUploadControllerTest.class);

	private static final String PUT_TEMPLATE = com.github.nikit.cpp.controllers.ImagePostTitleUploadController.PUT_TEMPLATE;
	private static final String GET_TEMPLATE = com.github.nikit.cpp.controllers.ImagePostTitleUploadController.GET_TEMPLATE;


	@WithUserDetails(TestConstants.USER_NIKITA)
    @Test
    public void putImage() throws Exception {
		byte[] img0 = {(byte)0xFF, (byte)0x01, (byte)0x1A};
        String url0 = putImage(img0);
		
		byte[] img1 = {(byte)0xAA, (byte)0xBB, (byte)0xCC, (byte)0xDD, (byte)0xCC};
		String url1 = putImage(img1);

		Assert.assertNotEquals(url0, url1);
    }

	@Test
	public void getUnexistingImage() throws Exception {
		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get(GET_TEMPLATE, "a979054b-8c9d-4df8-983e-6abc57c2aed6", "png")
		)
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("data not found"))
				.andExpect(jsonPath("$.message").value("post title image with id 'a979054b-8c9d-4df8-983e-6abc57c2aed6' not found"))
				.andReturn()
				;
	}

	private String putImage(byte[] bytes) throws Exception {
		MockMultipartFile imgPart = new MockMultipartFile(ImagePostTitleUploadController.IMAGE_PART, "lol.png", "image/png", bytes);
		MvcResult mvcResult = mockMvc.perform(
		MockMvcRequestBuilders.fileUpload(PUT_TEMPLATE)
				.file(imgPart).with(csrf())
		)
				.andExpect(status().isOk())
				.andReturn()
		;
		AbstractImageUploadController.ImageResponse imageResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), AbstractImageUploadController.ImageResponse.class);
		String urlResponse = imageResponse.getUrl();

		LOGGER.info("responsed image url: {}", urlResponse);

		MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get(urlResponse)
        )
                .andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/png"))
				.andReturn()
				;
		byte[] content = result.getResponse().getContentAsByteArray();
		
		Assert.assertArrayEquals(bytes, content);

		return urlResponse;
    }
}