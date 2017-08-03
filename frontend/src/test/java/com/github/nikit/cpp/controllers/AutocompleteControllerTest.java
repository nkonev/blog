package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.AbstractUtTestRunner;
import com.github.nikit.cpp.Constants;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by nik on 27.05.17.
 */
public class AutocompleteControllerTest extends AbstractUtTestRunner {

    private static final String PREFIX = Constants.Uls.API;

    @Test
    public void testAutoComplete() throws Exception {
        mockMvc.perform(post(PREFIX + Constants.Uls.REPOPULATE).with(csrf())).andExpect(status().isOk());

        mockMvc.perform(get(PREFIX + Constants.Uls.AUTOCOMPLETE).param("prefix", "Un"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].value", hasItems("United Arab Emirates", "United Kingdom", "United States")))
                .andDo(document("autocompleteManual"))
        ;
    }

    @Test
    public void testAutoCompleteConcatRestDocsToAdoc() throws Exception {
        mockMvc.perform(post(PREFIX + Constants.Uls.REPOPULATE).with(csrf())).andExpect(status().isOk());

        mockMvc.perform(get(PREFIX + Constants.Uls.AUTOCOMPLETE).param("prefix", "Slov"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].value", hasItems("Slovakia", "Slovenia")))
                .andDo(document(Constants.Swagger.AUTOCOMPLETE, preprocessResponse(prettyPrint())))
        ;
    }
}
