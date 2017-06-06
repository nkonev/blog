package com.github.nikit.cpp.integration;

import com.github.nikit.cpp.Launcher;
import org.junit.Test;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

/**
 * Created by nik on 06.06.17.
 */
public class FirstIT {
    @Test
    public void testOne() throws Exception {
        Launcher.main(new String[]{});

        open("http://127.0.0.1:9080/static/index.html");
        $("#countries-list").setValue("U");
        $(".ui-autocomplete .ui-menu-item div").shouldHave(text("Uganda"));

        //TimeUnit.SECONDS.sleep(9999);
    }
}
