package com.example;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AppTest {

    @Test
    void runtimeReportsTheActiveJdk() {
        assertTrue(App.runtime().contains(System.getProperty("java.version")));
    }
}
