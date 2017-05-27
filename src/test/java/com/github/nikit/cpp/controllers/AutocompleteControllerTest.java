package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.AbstractTestRunner;
import com.github.nikit.cpp.Constants;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by nik on 27.05.17.
 */
public class AutocompleteControllerTest extends AbstractTestRunner {

    @Test
    public void testAutoComplete() throws Exception {
        mockMvc.perform(post(Constants.Uls.REPOPULATE)).andExpect(status().isOk());

        mockMvc.perform(post(Constants.Uls.AUTOCOMPLETE).param("prefix", "Un"))
                .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasItems("United Arab Emirates", "United Kingdom", "United States")))
        ;
    }
}
