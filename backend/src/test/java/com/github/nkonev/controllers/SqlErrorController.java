package com.github.nkonev.controllers;

import com.github.nkonev.Constants;
import com.github.nkonev.TestConstants;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@RestController
public class SqlErrorController {

    @GetMapping(Constants.Uls.API + TestConstants.SQL_URL)
    public void getSql() {
        throw new DataIntegrityViolationException(TestConstants.SQL_QUERY);
    }
}
