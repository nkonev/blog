package com.github.nkonev.blog.controllers;

import com.github.nkonev.blog.AbstractUtTestRunner;
import com.github.nkonev.blog.TestConstants;
import org.junit.Test;
import org.springframework.security.test.context.support.WithUserDetails;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RiakControllerTest extends AbstractUtTestRunner {

    @WithUserDetails(TestConstants.USER_ALICE)
    @Test
    public void testUserCanAddFile() throws Exception {
        mockMvc.perform(post("/riak/store?data=ololo12").with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/riak/store"))
                .andExpect(status().isOk())
        .andExpect(content().string("ololo12"))
        ;

    }
}
