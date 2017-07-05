package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.PageUtils;
import com.github.nikit.cpp.dto.UserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
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
    public ResponseEntity<HelloweenResponse> hello(Principal principal) {

        return new ResponseEntity<>(new HelloweenResponse("Happy Halloween, " + principal.getName() + "!"), HttpStatus.OK);
    }

    public static class HelloweenResponse {
        private String message;
        public HelloweenResponse(String message) {
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
    public UserDTO checkAuthenticated(Principal principal) {
        return new UserDTO(-1L, principal.getName());
    }

    private static final List<UserDTO> USER_DTO_LIST;
    static {
        USER_DTO_LIST = new ArrayList<>();
        for (int i=0; i<100000; ++i){
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

    public static class Count{
        private int pages;
        private int count;

        public Count() { }

        public Count(int pages, int count) {
            this.pages = pages;
            this.count = count;
        }

        public int getPages() {
            return pages;
        }

        public void setPages(int pages) {
            this.pages = pages;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
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