package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.TestConstants;
import com.github.nikit.cpp.dto.UserAccountDetailsDTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserAccountDetailsDTOController {
    @GetMapping(Constants.Uls.API + TestConstants.USER_DETAILS)
    public UserAccountDetailsDTO getUserDetails() {
        return new UserAccountDetailsDTO();
    }

}
