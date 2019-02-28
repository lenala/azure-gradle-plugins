package lenala.azure.gradle.webapp.handlers;

import lenala.azure.gradle.webapp.DeployTask;
import lenala.azure.gradle.webapp.configuration.DeployTarget;
import org.gradle.api.GradleException;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;

public class ZipArtifactHandlerImpl extends ArtifactHandlerBase {
    private static final int DEFAULT_MAX_RETRY_TIMES = 3;

    public ZipArtifactHandlerImpl(final DeployTask task) {
        super(task);
    }

    @Override
    public void publish(DeployTarget target) throws GradleException, IOException {
        prepareResources();
        assureStagingDirectoryNotEmpty();

        File zipFile = getZipFile();
        task.getLogger().info(String.format(DEPLOY_START, target.getName()));

        // Add retry logic here to avoid Kudu's socket timeout issue.
        int retryCount = 0;
        while (retryCount < DEFAULT_MAX_RETRY_TIMES) {
            retryCount += 1;
            try {
                target.zipDeploy(zipFile);
                task.getLogger().quiet(String.format(DEPLOY_FINISH, target.getDefaultHostName()));
                return;
            } catch (Exception e) {
                task.getLogger().quiet(
                        String.format("Exception occurred when deploying the zip package: %s, " +
                                              "retrying immediately (%d/%d)", e.getMessage(), retryCount, DEFAULT_MAX_RETRY_TIMES));
            }
        }
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
