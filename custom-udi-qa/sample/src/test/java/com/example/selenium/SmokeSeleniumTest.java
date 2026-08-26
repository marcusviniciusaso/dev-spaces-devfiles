package com.example.selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmokeSeleniumTest {

    private WebDriver driver;

    @BeforeEach
    void setUp() {
        driver = WebDriverFactory.createHeadlessChrome();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void shouldLoadLocalPageAndValidateTitle() {
        Path htmlPath = Paths.get("src/test/resources/test-page.html").toAbsolutePath();
        driver.get("file://" + htmlPath);

        assertEquals("Dev Spaces Selenium Test", driver.getTitle());
    }

    @Test
    void shouldFindElementOnPage() {
        Path htmlPath = Paths.get("src/test/resources/test-page.html").toAbsolutePath();
        driver.get("file://" + htmlPath);

        WebElement heading = driver.findElement(By.id("main-heading"));
        assertTrue(heading.isDisplayed());
        assertEquals("Hello from Dev Spaces!", heading.getText());
    }
}
