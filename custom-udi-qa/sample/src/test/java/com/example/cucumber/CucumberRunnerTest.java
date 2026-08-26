package com.example.cucumber;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CucumberRunnerTest {

    @Test
    void runCucumberScenarios() {
        String[] args = {
            "--glue", "com.example.cucumber",
            "--plugin", "pretty",
            "src/test/resources/com/example/cucumber"
        };
        byte exitCode = io.cucumber.core.cli.Main.run(
            args, Thread.currentThread().getContextClassLoader());
        assertEquals(0, exitCode, "Cucumber scenarios failed");
    }
}
