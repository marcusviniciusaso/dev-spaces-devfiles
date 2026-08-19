package com.example.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public final class WebDriverFactory {

    private WebDriverFactory() {}

    public static WebDriver createHeadlessChrome() {
        String driverPath = System.getProperty("webdriver.chrome.driver");
        if (driverPath == null || driverPath.isBlank()) {
            driverPath = System.getenv("WEBDRIVER_CHROME_DRIVER");
        }
        if (driverPath != null && !driverPath.isBlank()) {
            System.setProperty("webdriver.chrome.driver", driverPath);
        }

        ChromeOptions options = new ChromeOptions();
        options.addArguments(
            "--headless=new",
            "--no-sandbox",
            "--disable-dev-shm-usage",
            "--disable-gpu"
        );

        String chromeBin = System.getenv("CHROME_BIN");
        if (chromeBin != null && !chromeBin.isBlank()) {
            options.setBinary(chromeBin);
        }

        return new ChromeDriver(options);
    }
}
