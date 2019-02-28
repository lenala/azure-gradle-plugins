package lenala.azure.gradle.webapp.handlers;

import lenala.azure.gradle.webapp.configuration.DeployTarget;

public interface ArtifactHandler {
    void publish(DeployTarget deployTarget) throws Exception;
}
