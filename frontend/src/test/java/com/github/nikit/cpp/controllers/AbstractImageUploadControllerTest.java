package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.AbstractUtTestRunner;
import com.google.common.net.HttpHeaders;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractImageUploadControllerTest extends AbstractUtTestRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractImageUploadControllerTest.class);

    protected String putImage(String putUrlTemplate, MockMultipartFile mpf) throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                MockMvcRequestBuilders.fileUpload(putUrlTemplate)
                        .file(mpf).with(csrf())
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
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, mpf.getContentType()))
                .andReturn()
                ;
        byte[] content = result.getResponse().getContentAsByteArray();

        Assert.assertArrayEquals(mpf.getBytes(), content);

        return urlResponse;
    }

}
