package com.github.nikit.cpp;

/**
 * Created by nik on 06.06.17.
 */
public class IntegrationTestConstants {

    public static final String URL_PREFIX = "${custom.it.url.prefix}";
    public static final String USER = "${custom.it.user}";
    public static final String PASSWORD = "${custom.it.password}";
    public static final String USER_ID = "${custom.it.user.id}";

    public static class Pages {
        public static final String INDEX_HTML = "/";
        public static final String AUTOCOMPLETE = "/autocomplete";
        public static final String USERS_LIST = "/users";
        public static final String USER = "/user";
    }
}
