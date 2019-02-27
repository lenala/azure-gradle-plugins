package com.lenala.azure.gradle.webapp.handlers;

import com.lenala.azure.gradle.webapp.configuration.DeployTarget;

public interface ArtifactHandler {
    void publish(DeployTarget deployTarget) throws Exception;
}
