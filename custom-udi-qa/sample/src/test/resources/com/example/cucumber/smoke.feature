Feature: Smoke test with Chrome headless in Dev Spaces

  Scenario: Validate page title
    Given the test page is loaded
    When I check the page title
    Then the title should be "Dev Spaces Selenium Test"

  Scenario: Validate page content
    Given the test page is loaded
    Then the heading should contain "Hello from Dev Spaces!"
    And the feature list should have 3 items
