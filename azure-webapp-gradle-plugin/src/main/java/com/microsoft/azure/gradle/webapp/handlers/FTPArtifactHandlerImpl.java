/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.gradle.webapp.handlers;

import com.microsoft.azure.gradle.webapp.AzureWebAppExtension;
import com.microsoft.azure.gradle.webapp.DeployTask;
import com.microsoft.azure.gradle.webapp.configuration.DeployTarget;
import com.microsoft.azure.gradle.webapp.configuration.Deployment;
import com.microsoft.azure.gradle.webapp.configuration.FTPResource;
import com.microsoft.azure.gradle.webapp.helpers.FTPUploader;
import com.microsoft.azure.management.appservice.PublishingProfile;
import com.microsoft.azure.management.appservice.WebApp;
import org.apache.commons.io.FileUtils;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static com.microsoft.azure.gradle.webapp.helpers.CommonStringTemplates.PROPERTY_MISSING_TEMPLATE;

public class FTPArtifactHandlerImpl implements ArtifactHandler {
    private static final String DEFAULT_WEBAPP_ROOT = "/site/wwwroot";
    private static final int DEFAULT_MAX_RETRY_TIMES = 3;

    private DeployTask task;
    private AzureWebAppExtension azureWebAppExtension;
    private Logger logger = Logging.getLogger(FTPArtifactHandlerImpl.class);

    public FTPArtifactHandlerImpl(final DeployTask task) {
        this.task = task;
        azureWebAppExtension = task.getAzureWebAppExtension();
    }

    @Override
    public void publish(DeployTarget target) throws Exception {
        Deployment deployment = azureWebAppExtension.getDeployment();
        if (deployment != null && copyResourceToStageDirectory(deployment.getResources())) {
            uploadDirectoryToFTP();
        }
    }

    private boolean copyResourceToStageDirectory(final List<FTPResource> resources) {
        if (resources == null || resources.isEmpty()) {
            logger.quiet(String.format(PROPERTY_MISSING_TEMPLATE, "deployment.resources"));
            return false;
        }
        resources.forEach(item -> {
            doCopyResourceToStageDirectory(item);
        });
        return true;
    }

    private void doCopyResourceToStageDirectory(final FTPResource resource) {
        File file  = new File(resource.getSourcePath());
        if (!file.exists()) {
            logger.quiet(resource.getSourcePath() + " configured in deployment.resources does not exist.");
            return;
        }
        File destination = new File(Paths.get(getDeploymentStageDirectory(), resource.getTargetPath()).toString());
        try {
            if (file.isFile()) {
                FileUtils.copyFileToDirectory(file, destination);
            } else if (file.isDirectory()) {
                FileUtils.copyDirectoryToDirectory(file, destination);
            }
        } catch (IOException e) {
            logger.quiet("exception when copy resource: " + e.getLocalizedMessage());
        }
    }

    private void uploadDirectoryToFTP() throws Exception {
        final FTPUploader uploader = getUploader();
        final WebApp app = task.getWebApp();
        final PublishingProfile profile = app.getPublishingProfile();
        final String serverUrl = profile.ftpUrl().split("/", 2)[0];
        uploader.uploadDirectoryWithRetries(serverUrl,
                profile.ftpUsername(),
                profile.ftpPassword(),
                getDeploymentStageDirectory(),
                DEFAULT_WEBAPP_ROOT,
                DEFAULT_MAX_RETRY_TIMES);
    }

    private FTPUploader getUploader() {
        return new FTPUploader(logger);
    }

    private String getDeploymentStageDirectory() {
        String stageDirectory = Paths.get(getBuildDirectoryAbsolutePath(),
                "azure-webapps",
                azureWebAppExtension.getAppName()).toString();
        logger.quiet(stageDirectory);
        return stageDirectory;
    }

    public String getBuildDirectoryAbsolutePath() {
        return task.getProject().getBuildDir().getAbsolutePath();
    }
}
