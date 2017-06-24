package com.github.nikit.cpp;

/**
 * Created by nik on 06.06.17.
 */
public class IntegrationTestConstants {
    public static final String IMPLICITLY_WAIT_TIMEOUT = "${custom.implicitly.wait.timeout}";
    public static final String BROWSER = "${custom.browser}";
    public static final String URL_PREFIX = "${custom.selenium.url.prefix}";

    public static final String USER = "${custom.selenium.user}";
    public static final String PASSWORD = "${custom.selenium.password}";

    // TODO make "Page Object" pattern
    public static final String INDEX_HTML = "/index.html";
    public static final String AUTOCOMPLETE_HTML = "/autocomplete";

}
