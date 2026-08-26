package com.example.cucumber;

import com.example.selenium.WebDriverFactory;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SearchSteps {

    private WebDriver driver;

    @Before
    public void setUp() {
        driver = WebDriverFactory.createHeadlessChrome();
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Given("the test page is loaded")
    public void theTestPageIsLoaded() {
        Path htmlPath = Paths.get("src/test/resources/test-page.html").toAbsolutePath();
        driver.get("file://" + htmlPath);
    }

    @When("I check the page title")
    public void iCheckThePageTitle() {
        // title is checked in the Then step
    }

    @Then("the title should be {string}")
    public void theTitleShouldBe(String expectedTitle) {
        assertEquals(expectedTitle, driver.getTitle());
    }

    @Then("the heading should contain {string}")
    public void theHeadingShouldContain(String text) {
        WebElement heading = driver.findElement(By.id("main-heading"));
        assertTrue(heading.isDisplayed());
        assertEquals(text, heading.getText());
    }

    @Then("the feature list should have {int} items")
    public void theFeatureListShouldHaveItems(int count) {
        var items = driver.findElements(By.cssSelector("#features li"));
        assertEquals(count, items.size());
    }
}
