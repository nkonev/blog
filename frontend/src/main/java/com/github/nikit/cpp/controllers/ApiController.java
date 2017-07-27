package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.PageUtils;
import com.github.nikit.cpp.dto.UserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
public class ApiController {

    @RequestMapping(value = "/hello", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public HelloResponse hello(Principal principal) {

        return new HelloResponse("Hello, " + principal.getName() + "!");
    }

    public static class HelloResponse {
        private String message;
        public HelloResponse(String message) {
            this.message = message;
        }
        public String getMessage() {
            return message;
        }
        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * Отдаёт профиль текущего пользователя
     * @param principal
     * @return
     */
    @RequestMapping(value = "/profile", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public UserDTO checkAuthenticated(Principal principal) throws MalformedURLException {
        return new UserDTO(0L, principal.getName(), new URL("https://www.w3.org/html/logo/downloads/HTML5_Logo_512.png"));
    }

    private static final List<UserDTO> USER_DTO_LIST;
    static {
        USER_DTO_LIST = new ArrayList<>();
        for (int i=0; i<100001; ++i){
            USER_DTO_LIST.add(new UserDTO((long)i, "user"+i));
        }
    }

    public static class PageDTO {
        private int page; // starts with 0 accords Spring DATA Pageable
        private int size; // размер страницы


        public PageDTO() { }


        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }
    }

    @PostMapping(value = "/user")
    public Collection<UserDTO> getUsersPost(@NotNull PageDTO pageDTO) {
        int page = PageUtils.fixPage(pageDTO.getPage());
        int size = PageUtils.fixSize(pageDTO.getSize());
        return getUsers(page, size);
    }

    @GetMapping(value = "/user")
    public Collection<UserDTO> getUsersGet(
            @RequestParam(value = "page", required=false, defaultValue = "0") int page,
            @RequestParam(value = "size", required=false, defaultValue = "0") int size
    ) {
        page = PageUtils.fixPage(page);
        size = PageUtils.fixSize(size);
        return getUsers(page, size);
    }

    private Collection<UserDTO> getUsers(int page, int size) {
        return USER_DTO_LIST.stream().skip(page*size).limit(size).collect(Collectors.toList());
    }

    @GetMapping(value = "/user-count")
    public int getUsersCount() {
        return USER_DTO_LIST.size();
    }

    @GetMapping(value = "/user/{id}")
    public UserDTO getUser(@PathVariable("id") Long userId) {
        return USER_DTO_LIST.stream()
                .filter(userDTO -> userId.equals(userDTO.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("user with id="+ userId + " not found"));
    }
}