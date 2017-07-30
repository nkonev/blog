package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.exception.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;
import java.util.Map;

@RestControllerAdvice
public class ExceptionHandler {
    @org.springframework.web.bind.annotation.ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> badRequest(BadRequestException badRequest) {
       Map<String, String> b = Collections.singletonMap("message", badRequest.getMessage());
       return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(b);
    }
}
