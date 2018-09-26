/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.gradle.webapp.handlers;

import com.microsoft.azure.gradle.webapp.DeployTask;
import com.microsoft.azure.gradle.webapp.configuration.DeployTarget;
import org.gradle.api.GradleException;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;

public class ZipArtifactHandlerImpl extends ArtifactHandlerBase {

    public ZipArtifactHandlerImpl(final DeployTask task) {
        super(task);
    }

    @Override
    public void publish(DeployTarget target) throws GradleException, IOException {
        prepareResources();
        assureStagingDirectoryNotEmpty();

        target.zipDeploy(getZipFile());
    }

    protected File getZipFile() {
        final String stagingDirectoryPath = getDeploymentStagingDirectoryPath();
        final File zipFile = new File(stagingDirectoryPath + ".zip");
        final File stagingDirectory = new File(stagingDirectoryPath);

        ZipUtil.pack(stagingDirectory, zipFile);
        return zipFile;
    }
}
