package com.github.nkonev.blog.utils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class IpUtil {
    public static String getIpAddressFromRequestContext() {
        if (RequestContextHolder.getRequestAttributes() == null) {return "cannot-get-remote-addr";}
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return getIpAddress(request);
    }


    // http://www.mkyong.com/java/how-to-get-client-ip-address-in-java/
    // https://stackoverflow.com/a/29910902
    public static String getIpAddress(HttpServletRequest request) {
        String remoteAddr = "cannot-get-remote-addr";

        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }

        return remoteAddr;
    }


}
