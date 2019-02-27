package com.lenala.azure.gradle.webapp.configuration;

public enum DeployTargetType {
    WEBAPP("Web App"),
    SLOT("Deployment Slot");

//    FUNCTION("Function App");

    private final String value;

    DeployTargetType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}

