package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.AbstractUtTestRunner;
import com.github.nikit.cpp.TestConstants;
import com.google.common.net.HttpHeaders;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.core.Is.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ImageUserAvatarUploadControllerTest extends AbstractUtTestRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImageUserAvatarUploadControllerTest.class);

	private static final String PUT_IMAGE_URL_TEMPLATE = ImageUserAvatarUploadController.PUT_TEMPLATE;
	private static final String AVATAR_IMAGE_URL_TEMPLATE_WITH_FILENAME  = ImageUserAvatarUploadController.GET_TEMPLATE;

    @WithUserDetails(TestConstants.USER_ALICE)
    @Test
    public void putImage() throws Exception {

		byte[] img0 = {(byte)0xFF, (byte)0x01, (byte)0x1A};
        putImage(img0);
		
		byte[] img1 = {(byte)0xAA, (byte)0xBB, (byte)0xCC, (byte)0xDD, (byte)0xCC};
		putImage(img1);
    }

	@Test
	public void getUnexistingImage() throws Exception {

		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get(AVATAR_IMAGE_URL_TEMPLATE_WITH_FILENAME, "a979054b-8c9d-4df8-983e-6abc57c2aed6", "png")
		)
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("data not found"))
				.andExpect(jsonPath("$.message").value("avatar image with id 'a979054b-8c9d-4df8-983e-6abc57c2aed6' not found"))
				.andReturn()
				;
    }
	
	private void putImage(byte[] bytes) throws Exception {
		MockMultipartFile imgPart = new MockMultipartFile(ImagePostTitleUploadController.IMAGE_PART, "lol.png", "image/png", bytes);
		MvcResult mvcResult = mockMvc.perform(
		MockMvcRequestBuilders.fileUpload(PUT_IMAGE_URL_TEMPLATE)
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
				// .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/png")) // TODO header don't appears in browser
				.andReturn()
				;
		byte[] content = result.getResponse().getContentAsByteArray();
		
		Assert.assertArrayEquals(bytes, content);
    }
}