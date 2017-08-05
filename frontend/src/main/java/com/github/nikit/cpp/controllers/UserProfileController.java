package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.PageUtils;
import com.github.nikit.cpp.converter.UserAccountConverter;
import com.github.nikit.cpp.dto.UserAccountDTO;
import com.github.nikit.cpp.dto.UserAccountDetailsDTO;
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

    /**
     *
     * @param userAccount
     * @return current logged in profile
     */
    @GetMapping(value = Constants.Uls.PROFILE)
    public UserAccountDTO checkAuthenticated(@AuthenticationPrincipal UserAccountDetailsDTO userAccount) throws MalformedURLException {
        return UserAccountConverter.convertToUserAccountDTO(userAccount);
    }

    private static final List<UserAccountDTO> USER_DTO_LIST;
    static {
        USER_DTO_LIST = new ArrayList<>();
        for (int i=0; i<100001; ++i){
            USER_DTO_LIST.add(new UserAccountDTO((long)i, "user"+i));
        }
    }

    @GetMapping(value = "/user")
    public Collection<UserAccountDTO> getUsersGet(
            @RequestParam(value = "page", required=false, defaultValue = "0") int page,
            @RequestParam(value = "size", required=false, defaultValue = "0") int size
    ) {
        page = PageUtils.fixPage(page);
        size = PageUtils.fixSize(size);
        return getUsers(page, size);
    }

    private Collection<UserAccountDTO> getUsers(int page, int size) {
        return USER_DTO_LIST.stream().skip(page*size).limit(size).collect(Collectors.toList());
    }

    @GetMapping(value = "/user-count")
    public int getUsersCount() {
        return USER_DTO_LIST.size();
    }

    @GetMapping(value = "/user/{id}")
    public UserAccountDTO getUser(@PathVariable("id") Long userId) {
        return USER_DTO_LIST.stream()
                .filter(userDTO -> userId.equals(userDTO.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("user with id="+ userId + " not found"));
    }
}