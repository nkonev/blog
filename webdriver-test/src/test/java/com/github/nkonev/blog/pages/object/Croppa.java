package com.github.nkonev.blog.pages.object;

import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import java.util.concurrent.TimeUnit;

public class Croppa {
    public static void setImage(String absoluteFilePath) {
        WebDriver driver = WebDriverRunner.getWebDriver();
        {
            final By croppaId = By.cssSelector(".croppa-container svg");
            new FluentWait<>(driver).withMessage("croppa remove button element was not found")
                    .withTimeout(10, TimeUnit.SECONDS)
                    .pollingEvery(1, TimeUnit.SECONDS)
                    .until(webDriver -> ExpectedConditions.visibilityOf(driver.findElement(croppaId)));
            driver.findElement(croppaId).click();
        }

        {
            final By croppaId = By.cssSelector(".croppa-container input");
            new FluentWait<>(driver).withMessage("croppa upload element was not found")
                    .withTimeout(10, TimeUnit.SECONDS)
                    .pollingEvery(1, TimeUnit.SECONDS)
                    .until(webDriver -> ExpectedConditions.visibilityOf(driver.findElement(croppaId)));
            driver.findElement(croppaId).sendKeys(absoluteFilePath);
        }
    }
}
