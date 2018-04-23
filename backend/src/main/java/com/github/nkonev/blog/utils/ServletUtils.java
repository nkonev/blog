package com.github.nkonev.blog.utils;

import javax.servlet.http.HttpServletRequest;

public class ServletUtils {
    public static String nullToEmpty(String s){
        if (s == null) {
            return "";
        } else {
            return s;
        }
    }

    public static String getQuery(HttpServletRequest request) {
        if (request.getQueryString() == null) {
            return "";
        } else {
            return "?" + request.getQueryString();
        }
    }
}
