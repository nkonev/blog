package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.AbstractUtTestRunner;
import com.github.nikit.cpp.Constants;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by nik on 27.05.17.
 */
public class AutocompleteControllerTest extends AbstractUtTestRunner {

    @Test
    public void testAutoComplete() throws Exception {
        mockMvc.perform(post(Constants.Uls.API + Constants.Uls.REPOPULATE)).andExpect(status().isOk());

        mockMvc.perform(get(Constants.Uls.API + Constants.Uls.AUTOCOMPLETE).param("prefix", "Un"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasItems("United Arab Emirates", "United Kingdom", "United States")))
                .andDo(document("autocompleteManual"))
        ;
    }

    @Test
    public void testAutoCompleteConcatRestDocsToAdoc() throws Exception {
        mockMvc.perform(post(Constants.Uls.API + Constants.Uls.REPOPULATE)).andExpect(status().isOk());

        mockMvc.perform(get(Constants.Uls.API + Constants.Uls.AUTOCOMPLETE).param("prefix", "Slov"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasItems("Slovakia", "Slovenia")))
                .andDo(document(Constants.Swagger.AUTOCOMPLETE, preprocessResponse(prettyPrint())))
        ;
    }
}
