package com.github.nkonev.junit.matcher;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class RegexpMatcher extends BaseMatcher<String> {
    public RegexpMatcher(String regexp) {
        this.regexp = regexp;
    }
    private final String regexp;
    @Override
    public boolean matches(Object item) {
        if (item instanceof String) {
            return ((String)item).matches(regexp);
        } else {
            return false;
        }
    }

    @Override
    public void describeMismatch(Object item, Description mismatchDescription) {
        mismatchDescription.appendText("a ").appendValue(item).appendText(" don't match to regexp ").appendValue(regexp);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("Comparing with regexp ").appendValue(regexp);
    }

    public static RegexpMatcher regexp(String regexp) {
        return new RegexpMatcher(regexp);
    }
}
