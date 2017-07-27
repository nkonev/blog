package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.dto.UserDTO;
import com.github.nikit.cpp.entity.UserAccount;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#mvc-authentication-principal
@RequestMapping(Constants.Uls.API)
@RestController
public class UserAccountController {

    public static UserDTO convertToUserDto(UserAccount userAccount) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(userAccount.getId());
        userDTO.setLogin(userAccount.getUsername());
        userDTO.setAvatar(userAccount.getAvatar());
        return userDTO;
    }

    @GetMapping("/user/profile2")
    public UserDTO getProfile2(@AuthenticationPrincipal UserAccount userAccount) {
        return convertToUserDto(userAccount);
    }
}
