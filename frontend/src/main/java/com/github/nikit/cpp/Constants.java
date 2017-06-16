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
        public static final String API = "/api";
        public static final String API_PUBLIC = "/api/public";
        public static final String REPOPULATE = "/repopulate";
        public static final String AUTOCOMPLETE = "/autocomplete";
    }

    public static class Swagger {
        public static final String AUTOCOMPLETE = "autocompletePost";
    }
}
