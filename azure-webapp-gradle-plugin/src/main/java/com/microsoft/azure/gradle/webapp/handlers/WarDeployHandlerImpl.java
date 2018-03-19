/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.gradle.webapp.handlers;

import com.microsoft.azure.gradle.webapp.DeployTask;

import java.io.File;

public class WarDeployHandlerImpl implements ArtifactHandler {
    private DeployTask task;

    public WarDeployHandlerImpl(final DeployTask task) {
        this.task = task;
    }

    @Override
    public void publish() throws Exception {
        String urlPath = task.getAzureWebAppExtension().getAppServiceOnLinux().getUrlPath();
        task.getLogger().quiet(urlPath);
        if (task.getAzureWebAppExtension().getAppServiceOnLinux().getUrlPath() != null) {
            task.getWebApp().update().withAppSetting("SCM_TARGET_PATH", "webapps/" + task.getAzureWebAppExtension().getAppServiceOnLinux().getUrlPath()).apply();
        }
        task.getWebApp().warDeploy(new File(task.getAzureWebAppExtension().getTarget()));
    }
}
