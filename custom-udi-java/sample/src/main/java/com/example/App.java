package com.example;

public class App {

    public static String runtime() {
        return "Java " + System.getProperty("java.version")
                + " (" + System.getProperty("java.vendor") + ") em "
                + System.getProperty("java.home");
    }

    public static void main(String[] args) {
        System.out.println(runtime());
    }
}
