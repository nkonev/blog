package com.github.nkonev.controllers;

import com.github.nkonev.Constants;
import com.github.nkonev.TestConstants;
import com.github.nkonev.dto.UserAccountDetailsDTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@RestController
public class UserAccountDetailsDTOController {
    @GetMapping(Constants.Uls.API + TestConstants.USER_DETAILS)
    public UserAccountDetailsDTO getUserDetails() {
        return new UserAccountDetailsDTO();
    }

}
