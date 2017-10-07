package com.github.nkonev;

/**
 * Created by nik on 06.06.17.
 */
public class IntegrationTestConstants {

    public static final String URL_PREFIX = "${custom.it.url.prefix}";
    // admin/admin
    public static final String USER = CommonTestConstants.USER;
    public static final String PASSWORD = CommonTestConstants.PASSWORD;
    public static final String USER_ID = CommonTestConstants.USER_ID;

    public static final long POST_WITH_COMMENTS = 2002;

    public static class Pages {
        public static final String INDEX_HTML = "/";
        public static final String AUTOCOMPLETE = "/autocomplete";
        public static final String USERS_LIST = "/users";
        public static final String USER = "/user";
    }
}
