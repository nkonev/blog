package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.PageUtils;
import com.github.nikit.cpp.dto.UserAccountDTO;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
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
     * Отдаёт профиль текущего пользователя
     * @param principal
     * @return
     */
    @RequestMapping(value = Constants.Uls.PROFILE, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public UserAccountDTO checkAuthenticated(Principal principal) throws MalformedURLException {
        return new UserAccountDTO(0L, principal.getName(), new URL("https://www.w3.org/html/logo/downloads/HTML5_Logo_512.png"));
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