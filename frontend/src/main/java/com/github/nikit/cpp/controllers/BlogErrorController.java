package com.github.nikit.cpp.controllers;

import com.github.nikit.cpp.Constants;
import com.github.nikit.cpp.dto.BlogError;
import com.github.nikit.cpp.dto.BlogErrorWithDebug;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.AbstractErrorController;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorViewResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @see org.springframework.boot.autoconfigure.web.BasicErrorController, it describes how to use both REST And ModelAndView handling depends on Accept header
 * @see "https://gist.github.com/jonikarppinen/662c38fb57a23de61c8b"
 */
@Controller
public class BlogErrorController extends AbstractErrorController {

    @Value("${debugResponse:false}")
    private boolean debug;

    private static final String PATH = "/error";

    public BlogErrorController(ErrorAttributes errorAttributes, List<ErrorViewResolver> errorViewResolvers) {
        super(errorAttributes, errorViewResolvers);
    }

    @RequestMapping(value = PATH, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public BlogError errorJson(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> errorAttributes = getErrorAttributes(request, debug);

        if (debug) {
            return new BlogErrorWithDebug(
                    response.getStatus(),
                    (String) errorAttributes.get("error"),
                    (String) errorAttributes.get("message"),
                    errorAttributes.get("timestamp").toString(),
                    (String) errorAttributes.get("exception"),
                    (String) errorAttributes.get("trace")
            );
        } else {
            return new BlogError(
                    response.getStatus(),
                    (String) errorAttributes.get("error"),
                    (String) errorAttributes.get("message"),
                    errorAttributes.get("timestamp").toString()
            );
        }
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }

    @RequestMapping(value = PATH, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
        HttpStatus status = getStatus(request);
        if (status.equals(HttpStatus.NOT_FOUND)) {
            // this is not found fallback which works when Accept text/html
            // NotFoundFallback for History API routing, e. g. for url http://127.0.0.1:8080/user/3
            response.setStatus(HttpServletResponse.SC_OK);
            return new ModelAndView(Constants.Uls.ROOT);
        }
        Map<String, Object> model = Collections.unmodifiableMap(getErrorAttributes(request, debug));
        response.setStatus(status.value());
        ModelAndView modelAndView = resolveErrorView(request, response, status, model);
        return (modelAndView == null ? new ModelAndView("error", model) : modelAndView);
    }

}
