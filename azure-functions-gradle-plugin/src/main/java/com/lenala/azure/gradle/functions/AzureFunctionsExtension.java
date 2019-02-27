package com.lenala.azure.gradle.functions;

import org.gradle.api.tasks.Input;

public class AzureFunctionsExtension {
    @Input
    private String authFile;

    public String getAuthFile() {
        return authFile;
    }
}
