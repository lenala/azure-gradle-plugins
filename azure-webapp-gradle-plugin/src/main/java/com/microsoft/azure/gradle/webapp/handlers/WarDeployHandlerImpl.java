/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.gradle.webapp.handlers;

import com.microsoft.azure.gradle.webapp.DeployTask;
import org.gradle.api.GradleException;

import java.io.File;

public class WarDeployHandlerImpl implements ArtifactHandler {
    private DeployTask task;

    public WarDeployHandlerImpl(final DeployTask task) {
        this.task = task;
    }

    @Override
    public void publish() throws Exception {
        String urlPath;
        if (task.getAzureWebAppExtension().getAppServiceOnLinux() != null) {
           urlPath = task.getAzureWebAppExtension().getAppServiceOnLinux().getUrlPath();
        } else if (task.getAzureWebAppExtension().getAppServiceOnWindows() != null){
            urlPath = task.getAzureWebAppExtension().getAppServiceOnWindows().getUrlPath();
        } else {
            throw new GradleException("WARDEPLOY deployment type not available for Web Apps on Containers deployments");
        }
        task.getLogger().quiet(urlPath);
        task.getWebApp().update().withAppSetting("SCM_TARGET_PATH", "webapps/" + (urlPath == null ? "" : urlPath)).apply();
        task.getWebApp().warDeploy(new File(task.getAzureWebAppExtension().getTarget()));
    }
}
