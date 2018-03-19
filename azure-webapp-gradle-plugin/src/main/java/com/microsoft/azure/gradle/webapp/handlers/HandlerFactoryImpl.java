/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.gradle.webapp.handlers;

import com.microsoft.azure.gradle.webapp.DeployTask;
import com.microsoft.azure.gradle.webapp.configuration.AppServiceOnLinux;
import com.microsoft.azure.gradle.webapp.configuration.AppServiceOnWindows;
import com.microsoft.azure.gradle.webapp.configuration.DeploymentType;
import com.microsoft.azure.gradle.webapp.helpers.WebAppUtils;
import com.microsoft.azure.gradle.webapp.configuration.ContainerSettings;
import com.microsoft.azure.gradle.webapp.configuration.DockerImageType;
import com.microsoft.azure.management.appservice.JavaVersion;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

public class HandlerFactoryImpl extends HandlerFactory {
    public static final String RUNTIME_CONFIG_CONFLICT = "<javaVersion> is for Web App on Windows; " +
            "<containerSettings> is for Web App on Linux; they can't be specified at the same time.";
    public static final String NO_RUNTIME_HANDLER = "Not able to process the runtime stack configuration; " +
            "please check <javaVersion> or <containerSettings> tag in pom.xml";
    public static final String IMAGE_NAME_MISSING = "<imageName> not found within <containerSettings> tag.";
    public static final String DEPLOYMENT_TYPE_NOT_FOUND = "<deploymentType> is not configured.";
    public static final String UNKNOWN_DEPLOYMENT_TYPE = "Unknown value from <deploymentType> tag.";

    @Override
    public RuntimeHandler getRuntimeHandler(final DeployTask task) throws GradleException {
        AppServiceOnWindows appServiceOnWindows = task.getAzureWebAppExtension().getAppServiceOnWindows();

        AppServiceOnLinux appServiceOnLinux = task.getAzureWebAppExtension().getAppServiceOnLinux();
        final ContainerSettings containerSettings = task.getAzureWebAppExtension().getContainerSettings();
        // No configuration is specified
        if (appServiceOnLinux == null && appServiceOnWindows == null && containerSettings == null ) {
            return new NullRuntimeHandlerImpl();
        }

//        // More than one configuration specified
//        if (javaVersion != null && containerSettings != null && !containerSettings.isEmpty()) {
//            throw new GradleException(RUNTIME_CONFIG_CONFLICT);
//        }

        if (appServiceOnWindows != null) {
            return new JavaRuntimeHandlerImpl(task);
        }

        if (appServiceOnLinux != null) {
            return new BuiltInImageRuntimeHandlerImpl(task);
        }

        final DockerImageType imageType = WebAppUtils.getDockerImageType(containerSettings);
        switch (imageType) {
            case PUBLIC_DOCKER_HUB:
                return new PublicDockerHubRuntimeHandlerImpl(task);
            case PRIVATE_DOCKER_HUB:
                return new PrivateDockerHubRuntimeHandlerImpl(task);
            case PRIVATE_REGISTRY:
                return new PrivateRegistryRuntimeHandlerImpl(task);
            case NONE:
                throw new GradleException(IMAGE_NAME_MISSING);
        }

        throw new GradleException(NO_RUNTIME_HANDLER);
    }

    @Override
    public SettingsHandler getSettingsHandler(final Project project) throws GradleException {
        return new SettingsHandlerImpl(project);
    }

    @Override
    public ArtifactHandler getArtifactHandler(final DeployTask task) throws GradleException {
        if (DeploymentType.WARDEPLOY.equals(task.getAzureWebAppExtension().getDeploymentType())) {
            return new WarDeployHandlerImpl(task);
        }
//        if (task.getAzureWebAppExtension().getPackageUri() != null) {
//            return new WebDeployHandlerImpl(task);
//        } else {
            return new FTPArtifactHandlerImpl(task.getProject());
//        }
    }
}
