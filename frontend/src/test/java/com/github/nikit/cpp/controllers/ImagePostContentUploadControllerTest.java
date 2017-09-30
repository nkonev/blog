package com.github.nikit.cpp.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nikit.cpp.AbstractUtTestRunner;
import com.github.nikit.cpp.TestConstants;
import com.google.common.net.HttpHeaders;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Random;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ImagePostContentUploadControllerTest extends AbstractImageUploadControllerTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImagePostContentUploadControllerTest.class);

	private static final String PUT_TEMPLATE = ImagePostContentUploadController.PUT_TEMPLATE;
	private static final String GET_TEMPLATE = ImagePostContentUploadController.GET_TEMPLATE;

	@WithUserDetails(TestConstants.USER_NIKITA)
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

	@WithUserDetails(TestConstants.USER_NIKITA)
	@Test
	public void putImageWithWrongContentType() throws Exception {
		byte[] img0 = {(byte)0xFF, (byte)0x01, (byte)0x1A};
		MockMultipartFile mf0 = new MockMultipartFile(ImagePostTitleUploadController.IMAGE_PART, "lol-content.png", "image/ololo", img0);

		MvcResult mvcResult = mockMvc.perform(
				MockMvcRequestBuilders.fileUpload(PUT_TEMPLATE)
						.file(mf0).with(csrf())
		)
				.andExpect(status().isUnsupportedMediaType())
				.andExpect(jsonPath("$.error").value("unsupported media type"))
				.andExpect(jsonPath("$.message").value("Incompatible content type. Allowed: [image/png, image/jpg, image/jpeg]"))
				.andReturn()
				;
	}

	@WithUserDetails(TestConstants.USER_NIKITA)
	@Test
	public void putImageWithVeryBigSize() throws Exception {

		// in application.yml 2 Mb allowed. We try to POST 4 Mb
		byte[] img0 = new byte[1024 * 1024 * 4];
		new Random().nextBytes(img0);

		MockMultipartFile mf0 = new MockMultipartFile(ImagePostTitleUploadController.IMAGE_PART, "lol-content.png", "image/ololo", img0);

		MvcResult mvcResult = mockMvc.perform(
				MockMvcRequestBuilders.fileUpload(PUT_TEMPLATE)
						.file(mf0).with(csrf())
		)
				.andExpect(status().isPayloadTooLarge())
				.andExpect(jsonPath("$.error").value("payload too large"))
				.andExpect(jsonPath("$.message").value("Image must be <= 1048576 bytes"))
				.andReturn()
				;
	}


	@Test
	public void getUnexistingImage() throws Exception {

		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders.get(GET_TEMPLATE, "a979054b-8c9d-4df8-983e-6abc57c2aed6", "png")
		)
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("data not found"))
				.andExpect(jsonPath("$.message").value("post content image with id 'a979054b-8c9d-4df8-983e-6abc57c2aed6' not found"))
				.andReturn()
				;
	}
}