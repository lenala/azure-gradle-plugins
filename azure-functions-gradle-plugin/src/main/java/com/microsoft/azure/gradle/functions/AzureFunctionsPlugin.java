/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.gradle.functions;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class AzureFunctionsPlugin implements Plugin<Project> {
    public static final String AZURE_FUNCTIONS = "azurefunctions";

    public void apply(Project project) {
        AzureFunctionsExtension azureFunctionsExtension = new AzureFunctionsExtension(project);
        project.getExtensions().add(AZURE_FUNCTIONS, azureFunctionsExtension);
//        project.getTasks().create("add", AddTask.class, (task) -> {
//            task.setAzureFunctionsExtension(azureFunctionsExtension);
//        });
//        project.getTasks().create("package", PackageTask.class, (task) -> {
//            task.setAzureFunctionsExtension(azureFunctionsExtension);
//        });
//        project.getTasks().create("deploy", DeployTask.class, (task) -> {
//            task.setAzureFunctionsExtension(azureFunctionsExtension);
//        });
//        project.getTasks().create("run", RunTask.class, (task) -> {
//            task.setAzureFunctionsExtension(azureFunctionsExtension);
//        });
    }
}