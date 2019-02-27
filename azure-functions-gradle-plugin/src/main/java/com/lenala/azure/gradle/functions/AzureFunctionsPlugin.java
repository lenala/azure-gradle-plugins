package com.lenala.azure.gradle.functions;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class AzureFunctionsPlugin implements Plugin<Project> {
    public static final String AZURE_FUNCTIONS = "azurefunctions";

    public void apply(Project project) {
        AzureFunctionsExtension azureFunctionsExtension = new AzureFunctionsExtension();
        project.getExtensions().add(AZURE_FUNCTIONS, azureFunctionsExtension);
    }
}