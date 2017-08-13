package com.github.nikit.cpp;

/**
 * Created by nik on 06.06.17.
 */
public class IntegrationTestConstants {

    public static final String URL_PREFIX = "${custom.it.url.prefix}";
    public static final String USER = CommonTestConstants.USER;
    public static final String PASSWORD = CommonTestConstants.PASSWORD;
    public static final String USER_ID = CommonTestConstants.USER_ID;

    public static class Pages {
        public static final String INDEX_HTML = "/";
        public static final String AUTOCOMPLETE = "/autocomplete";
        public static final String USERS_LIST = "/users";
        public static final String USER = "/user";
    }
}
