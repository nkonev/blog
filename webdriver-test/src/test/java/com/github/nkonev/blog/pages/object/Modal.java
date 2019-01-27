package com.github.nkonev.blog.pages.object;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;

public class Modal {
    public static SelenideElement getValidModal(String expectedText) {
        SelenideElement modal = $(".v--modal-box");
        modal.waitUntil(Condition.visible, 5*1000);
        modal.shouldHave(text(expectedText));
        return modal;
    }

}
