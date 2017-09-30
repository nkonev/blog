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

public class ImageUserAvatarUploadControllerTest extends AbstractImageUploadControllerTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImageUserAvatarUploadControllerTest.class);

	private static final String PUT_TEMPLATE = ImageUserAvatarUploadController.PUT_TEMPLATE;
	private static final String GET_TEMPLATE  = ImageUserAvatarUploadController.GET_TEMPLATE;

    @WithUserDetails(TestConstants.USER_ALICE)
    @Test
    public void putImage() throws Exception {

		byte[] img0 = {(byte)0xFF, (byte)0x01, (byte)0x1A};
		MockMultipartFile mf0 = new MockMultipartFile(ImagePostTitleUploadController.IMAGE_PART, "lol-content.png", "image/png", img0);
		String url0 = super.putImage(PUT_TEMPLATE, mf0);

		byte[] img1 = {(byte)0xAA, (byte)0xBB, (byte)0xCC, (byte)0xDD, (byte)0xCC};
		MockMultipartFile mf1 = new MockMultipartFile(ImagePostTitleUploadController.IMAGE_PART, "lol-content.png", "image/png", img1);
		String url1 = super.putImage(PUT_TEMPLATE, mf1);

		Assert.assertNotEquals(url0, url1);
    }

	@Test
	public void getUnexistingImage() throws Exception {

		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get(GET_TEMPLATE, "a979054b-8c9d-4df8-983e-6abc57c2aed6", "png")
		)
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("data not found"))
				.andExpect(jsonPath("$.message").value("avatar image with id 'a979054b-8c9d-4df8-983e-6abc57c2aed6' not found"))
				.andReturn()
				;
    }
}