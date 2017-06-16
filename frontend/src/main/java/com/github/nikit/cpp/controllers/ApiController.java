package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.Constants;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

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

    public static class UserDTO {
        private String login;

        public UserDTO(String login) {
            this.login = login;
        }

        public UserDTO() { }

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }
    }

    @RequestMapping(value = "/profile", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public UserDTO checkAuthenticated(Principal principal) {
        return new UserDTO(principal.getName());
    }
}