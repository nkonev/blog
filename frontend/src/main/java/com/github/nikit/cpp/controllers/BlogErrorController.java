package com.github.nikit.cpp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @see org.springframework.boot.autoconfigure.web.BasicErrorController, it describes how to use both REST And ModelAndView handling depends on Accept header
 * @see "https://gist.github.com/jonikarppinen/662c38fb57a23de61c8b"
 */
@Controller
public class BlogErrorController implements ErrorController {

    @Value("${debugResponse:false}")
    private boolean debug;

    private static final String PATH = "/error";

    @Autowired
    private ErrorAttributes errorAttributes;

    @RequestMapping
    @ResponseBody
    public Map<String, Object> error(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> errorAttributes = getErrorAttributes(request, debug);

        Map<String, Object> map = new HashMap<>();

        map.put("status", response.getStatus());
        map.put("error", (String) errorAttributes.get("error"));
        map.put("message", (String) errorAttributes.get("message"));
        map.put("timeStamp", errorAttributes.get("timestamp").toString());
        if (debug) {
            map.put("exception", (String) errorAttributes.get("exception"));
            map.put("trace", (String) errorAttributes.get("trace"));
        }
        return map;
    }
    @Override
    public String getErrorPath() {
        return PATH;
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest request, boolean includeStackTrace) {
        RequestAttributes requestAttributes = new ServletRequestAttributes(request);
        return errorAttributes.getErrorAttributes(requestAttributes, includeStackTrace);
    }
}
