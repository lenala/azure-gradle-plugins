/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.gradle.webapp.handlers;

import com.google.common.io.Files;
import com.microsoft.azure.gradle.webapp.DeployTask;
import org.gradle.api.GradleException;

import java.io.File;

public class WarDeployHandlerImpl implements ArtifactHandler {
    private static final String FILE_IS_NOT_WAR = "The deployment file is not a war typed file.";
    private static final String FIND_WAR_FILE_FAIL = "Failed to find the war file: '%s'";
    private static final int DEFAULT_MAX_RETRY_TIMES = 3;
    private static final String UPLOAD_FAILURE = "Failed to deploy the war file to server, retrying immediately (%d/%d)";

    private DeployTask task;

    public WarDeployHandlerImpl(final DeployTask task) {
        this.task = task;
    }

    @Override
    public void publish() throws GradleException {
        String target = task.getAzureWebAppExtension().getTarget();
        if (target == null || target.isEmpty()) {
            target = task.getProject().getTasks().getByPath("war").getOutputs().getFiles().getAsPath();
        }

        File targetFile = new File(target);
        assureWarFileExisted(targetFile);

        String urlPath;
        if (task.getAzureWebAppExtension().getAppServiceOnLinux() != null) {
            urlPath = task.getAzureWebAppExtension().getAppServiceOnLinux().getUrlPath();
        } else if (task.getAzureWebAppExtension().getAppServiceOnWindows() != null) {
            urlPath = task.getAzureWebAppExtension().getAppServiceOnWindows().getUrlPath();
        } else {
            throw new GradleException("WARDEPLOY deployment type not available for Web Apps on Containers deployments");
        }
//        task.getWebApp().update().withAppSetting("SCM_TARGET_PATH", "webapps/" + (urlPath == null ? "" : urlPath)).apply();
        task.getLogger().quiet("War name is: " + target);
        urlPath = urlPath == null ? "" : urlPath;
        int retryCount = 0;
        task.getLogger().quiet("Starting to deploy the war file...");
        while (retryCount++ < DEFAULT_MAX_RETRY_TIMES) {
            try {
                task.getWebApp().warDeploy(targetFile, urlPath);
                return;
            } catch (Exception e) {
                task.getLogger().quiet(String.format(UPLOAD_FAILURE, retryCount, DEFAULT_MAX_RETRY_TIMES));
            }
        }
    }

    private void assureWarFileExisted(File war) throws GradleException {
        if (!Files.getFileExtension(war.getName()).equalsIgnoreCase("war")) {
            throw new GradleException(FILE_IS_NOT_WAR);
        }

        if (!war.exists() || !war.isFile()) {
            throw new GradleException(String.format(FIND_WAR_FILE_FAIL, war.getAbsolutePath()));
        }
    }
}
