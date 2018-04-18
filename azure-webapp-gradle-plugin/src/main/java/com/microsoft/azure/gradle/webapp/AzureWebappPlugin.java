/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.gradle.webapp;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static com.microsoft.azure.gradle.webapp.AzureWebAppExtension.WEBAPP_EXTENSION_NAME;

public class AzureWebappPlugin implements Plugin<Project> {
    public void apply(Project project) {
        AzureWebAppExtension azureWebAppExtension = new AzureWebAppExtension(project);
        project.getExtensions().add(WEBAPP_EXTENSION_NAME, azureWebAppExtension);
        project.getTasks().create(DeployTask.TASK_NAME, DeployTask.class, (task) -> {
            task.setAzureWebAppExtension(azureWebAppExtension);
        });
    }
}