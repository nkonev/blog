package com.github.nikit.cpp.controllers;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ImagePostTitleUploadControllerTest extends AbstractImageUploadControllerTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImagePostTitleUploadControllerTest.class);

	private static final String POST_TEMPLATE = com.github.nikit.cpp.controllers.ImagePostTitleUploadController.POST_TEMPLATE;
	private static final String GET_TEMPLATE = com.github.nikit.cpp.controllers.ImagePostTitleUploadController.GET_TEMPLATE;


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

	@Override
	protected String postTemplate() {
		return POST_TEMPLATE;
	}
}