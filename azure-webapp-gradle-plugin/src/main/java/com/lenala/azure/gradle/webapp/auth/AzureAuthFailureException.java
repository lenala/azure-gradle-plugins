package com.lenala.azure.gradle.webapp.auth;

public class AzureAuthFailureException extends Exception {
    public AzureAuthFailureException(String message) {
        super(message);
    }
}
