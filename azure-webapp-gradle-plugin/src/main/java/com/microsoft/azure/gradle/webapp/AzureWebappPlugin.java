/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.gradle.webapp;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class AzureWebappPlugin implements Plugin<Project> {
    public void apply(Project project) {
        AzureWebAppExtension azureWebAppExtension = new AzureWebAppExtension(project);
        project.getExtensions().add("deploy", azureWebAppExtension);
        project.getTasks().create("deploy", DeployTask.class, (task) -> {
            task.setAzureWebAppExtension(azureWebAppExtension);
        });
    }
}