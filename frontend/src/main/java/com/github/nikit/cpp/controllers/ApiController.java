package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.dto.UserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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

    private static final List<UserDTO> USER_DTO_LIST = Arrays.asList(
            new UserDTO(1L, "user1"),
            new UserDTO(2L, "user2"),
            new UserDTO(3L, "user3"),
            new UserDTO(4L, "user4"),
            new UserDTO(10L, "user10")
    );


    @GetMapping(value = "/user")
    public Collection<UserDTO> getUsers() {
        return USER_DTO_LIST;
    }

    @GetMapping(value = "/user/{id}")
    public UserDTO getUser(@PathVariable("id") Long userId) {
        return USER_DTO_LIST.stream()
                .filter(userDTO -> userId.equals(userDTO.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("user with id="+ userId + " nt found"));
    }

}