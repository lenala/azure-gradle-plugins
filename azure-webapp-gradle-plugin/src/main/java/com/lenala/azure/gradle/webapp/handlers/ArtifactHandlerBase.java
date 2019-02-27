package com.lenala.azure.gradle.webapp.handlers;

import com.lenala.azure.gradle.webapp.DeployTask;
import com.lenala.azure.gradle.webapp.configuration.FTPResource;
import org.apache.commons.io.FileUtils;
import org.gradle.api.GradleException;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public abstract class ArtifactHandlerBase implements ArtifactHandler {
    protected static final String DEPLOY_START = "Trying to deploy artifact to %s...";
    protected static final String DEPLOY_FINISH = "Successfully deployed the artifact to https://%s";

    protected DeployTask task;

    protected ArtifactHandlerBase(@Nonnull final DeployTask task) {
        this.task = task;
    }

    protected void assureStagingDirectoryNotEmpty() throws GradleException {
        final String stagingDirectoryPath = getDeploymentStagingDirectoryPath();
        final File stagingDirectory = new File(stagingDirectoryPath);
        final File[] files = stagingDirectory.listFiles();
        if (!stagingDirectory.exists() || !stagingDirectory.isDirectory() || files == null || files.length == 0) {
            throw new GradleException(String.format("Staging directory: '%s' is empty.",
                    stagingDirectory.getAbsolutePath()));
        }
    }

    protected String getDeploymentStagingDirectoryPath() {
        String stageDirectory = Paths.get(getBuildDirectoryAbsolutePath(),
                "azure-webapps",
                task.getAzureWebAppExtension().getAppName()).toString();
        task.getLogger().quiet(stageDirectory);
        return stageDirectory;
    }

    protected void prepareResources() throws IOException {
        final List<FTPResource> resources = task.getAzureWebAppExtension().getDeployment().getResources();

        if (resources != null && !resources.isEmpty()) {
            resources.forEach(item -> {
                doCopyResourceToStageDirectory(item);
            });
        }
    }

    private void doCopyResourceToStageDirectory(final FTPResource resource) {
        File file  = new File(resource.getSourcePath());
        if (!file.exists()) {
            task.getLogger().quiet(resource.getSourcePath() + " configured in deployment.resources does not exist.");
            return;
        }
        File destination = new File(Paths.get(getDeploymentStagingDirectoryPath(), resource.getTargetPath()).toString());
        try {
            if (file.isFile()) {
                FileUtils.copyFileToDirectory(file, destination);
            } else if (file.isDirectory()) {
                FileUtils.copyDirectoryToDirectory(file, destination);
            }
        } catch (IOException e) {
            task.getLogger().quiet("exception when copy resource: " + e.getLocalizedMessage());
        }
    }

    public String getBuildDirectoryAbsolutePath() {
        return task.getProject().getBuildDir().getAbsolutePath();
    }
}
