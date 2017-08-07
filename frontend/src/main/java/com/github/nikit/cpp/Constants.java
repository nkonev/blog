package com.github.nikit.cpp;

/**
 * Created by nik on 23.05.17.
 */
public class Constants {
    public static final String KEY = "autocomplete";
    public static final String STOP_SYMBOL = "%";
    public static final int STEP = 42;
    public static final int COUNT = 50;
    public static final String FRONTEND_RESOURCES = "${custom.frontend.resources}";

    public static class Uls {
        public static final String ROOT = "/";
        public static final String API = "/api";
        public static final String ADMIN = "/admin";
        public static final String POST = "/post";
        public static final String PROFILE = "/profile";
        public static final String REPOPULATE = "/repopulate";
        public static final String AUTOCOMPLETE = "/autocomplete";
        public static final String MY = "/my";
        public static final String POST_ID = "/{postId}";
        public static final String COMMENT_ID = "/{commentId}";
        public static final String COMMENT = "/comment";
        public static final String REGISTER = "/register";
        public static final String CONFIRM = "/confirm";
        public static final String UUID = "uuid";
        public static final String RESEND_CONFIRMATION_EMAIL = "/resend-confirmation-email";
    }

    public static class PathVariables {
        public static final String POST_ID = "postId";
        public static final String COMMENT_ID = "commentId";
    }

    public static class Swagger {
        public static final String AUTOCOMPLETE = "autocompletePost";
    }

    public static class Schemas {
        public static final String AUTH = "auth";
        public static final String POSTS = "posts";
    }
}
