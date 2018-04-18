/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.gradle.webapp.handlers;

import com.microsoft.azure.gradle.webapp.AzureWebAppExtension;
import com.microsoft.azure.gradle.webapp.DeployTask;
import com.microsoft.azure.gradle.webapp.helpers.FTPUploader;
import com.microsoft.azure.management.appservice.PublishingProfile;
import com.microsoft.azure.management.appservice.WebApp;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static com.microsoft.azure.gradle.webapp.AzureWebAppExtension.WEBAPP_EXTENSION_NAME;

public class FTPArtifactHandlerImpl implements ArtifactHandler {
    private static final String DEFAULT_WEBAPP_ROOT = "/site/wwwroot" + "/webapps";
    private static final int DEFAULT_MAX_RETRY_TIMES = 3;

    private Project project;
    private AzureWebAppExtension azureWebAppExtension;
    private Logger logger = Logging.getLogger(FTPArtifactHandlerImpl.class);

    public FTPArtifactHandlerImpl(final Project project) {
        this.project = project;
        azureWebAppExtension = (AzureWebAppExtension) project.getExtensions().getByName(WEBAPP_EXTENSION_NAME);
    }

    @Override
    public void publish() throws Exception {
        copyResourceToStageDirectory();
        uploadDirectoryToFTP();
    }

    private void copyResourceToStageDirectory() throws IOException {
        String target = azureWebAppExtension.getTarget();
        if (target == null || target.isEmpty()) {
            target = project.getTasks().getByPath("war").getOutputs().getFiles().getAsPath();
        }
        logger.quiet("War name is: " + target);
//        Files.copy(Paths.get(target), Paths.get(task.getDeploymentStageDirectory()));
        FileUtils.copyFileToDirectory(new File(target), new File(getDeploymentStageDirectory()));
//        Utils.copyResources(/*mojo.getProject()*/null,
//                task.getSession(),
//                mojo.getMavenResourcesFiltering(),
//                resources,
//                mojo.getDeploymentStageDirectory());
    }

//    protected void copyResourcesToStageDirectory(final List<Resource> resources) throws IOException {

//        Utils.copyResources(/*mojo.getProject()*/null,
//                task.getSession(),
//                mojo.getMavenResourcesFiltering(),
//                resources,
//                mojo.getDeploymentStageDirectory());
//    }

    private void uploadDirectoryToFTP() throws Exception {
        final FTPUploader uploader = getUploader();
        final WebApp app = ((DeployTask) project.getTasks().getByPath(DeployTask.TASK_NAME)).getWebApp();
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

    private void copyResource() {

    }

    private String getDeploymentStageDirectory() {
        String stageDirectory = Paths.get(getBuildDirectoryAbsolutePath(),
                "azure-webapps",
                azureWebAppExtension.getAppName()).toString();
        logger.quiet(stageDirectory);
        return stageDirectory;
    }

    public String getBuildDirectoryAbsolutePath() {
        return project.getBuildDir().getAbsolutePath();
    }
}
