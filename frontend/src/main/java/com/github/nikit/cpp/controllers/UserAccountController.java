package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.dto.UserAccountDTO;
import com.github.nikit.cpp.entity.UserAccount;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#mvc-authentication-principal
@RequestMapping(Constants.Uls.API)
@RestController
public class UserAccountController {

    public static UserAccountDTO convertToUserDto(UserAccount userAccount) {
        UserAccountDTO userDTO = new UserAccountDTO();
        userDTO.setId(userAccount.getId());
        userDTO.setLogin(userAccount.getUsername());
        userDTO.setAvatar(userAccount.getAvatar());
        return userDTO;
    }

    @GetMapping("/user/profile2")
    public UserAccountDTO getProfile2( //fixme
                                       UserAccount userAccount) {
        return convertToUserDto(userAccount);
    }

    @GetMapping(Constants.Uls.ADMIN)
    public String hello() {
        return "hello";
    }

}
