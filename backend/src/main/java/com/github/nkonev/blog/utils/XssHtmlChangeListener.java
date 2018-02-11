package com.github.nkonev.blog.utils;

import org.owasp.html.HtmlChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class XssHtmlChangeListener implements HtmlChangeListener<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger("XSS");

    @Override
    public void discardedTag(Object context, String elementName) {
        LOGGER.warn("" + context + ", elementName='" + elementName + "'");
    }

    @Override
    public void discardedAttributes(Object context, String tagName, String... attributeNames) {
        LOGGER.warn("" + context + " tagName='"+tagName+"' , attributeNames=" + Arrays.toString(attributeNames) + "");
    }
}