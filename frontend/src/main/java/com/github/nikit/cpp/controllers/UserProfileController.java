package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.PageUtils;
import com.github.nikit.cpp.converter.UserAccountConverter;
import com.github.nikit.cpp.dto.UserAccountDTO;
import com.github.nikit.cpp.dto.UserAccountDetailsDTO;
import com.github.nikit.cpp.repo.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by nik on 08.06.17.
 */
@RequestMapping(Constants.Uls.API)
@RestController
@PreAuthorize("isAuthenticated()")
public class UserProfileController {

    @Autowired
    private UserAccountRepository userAccountRepository;

    /**
     *
     * @param userAccount
     * @return current logged in profile
     */
    @GetMapping(value = Constants.Uls.PROFILE)
    public UserAccountDTO checkAuthenticated(@AuthenticationPrincipal UserAccountDetailsDTO userAccount) throws MalformedURLException {
        return UserAccountConverter.convertToUserAccountDTO(userAccount);
    }

    @GetMapping(value = "/user")
    public Collection<UserAccountDTO> getUsersGet(
            @RequestParam(value = "page", required=false, defaultValue = "0") int page,
            @RequestParam(value = "size", required=false, defaultValue = "0") int size
    ) {
        PageRequest springDataPage = new PageRequest(PageUtils.fixPage(page), PageUtils.fixSize(size), Sort.Direction.ASC, "id");

        return userAccountRepository.findAll(springDataPage).getContent().stream().map(UserAccountConverter::convertToUserAccountDTO).collect(Collectors.toList());
    }

    @GetMapping(value = "/user-count")
    public long getUsersCount() {
        return userAccountRepository.count();
    }

    @GetMapping(value = "/user/{id}")
    public UserAccountDTO getUser(@PathVariable("id") Long userId) {

        return userAccountRepository.getById(userId).map(UserAccountConverter::convertToUserAccountDTO)
                .orElseThrow(() -> new RuntimeException("user with id="+ userId + " not found"));
    }
}