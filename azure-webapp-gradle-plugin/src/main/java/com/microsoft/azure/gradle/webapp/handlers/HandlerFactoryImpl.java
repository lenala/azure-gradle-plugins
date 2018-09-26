/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.gradle.webapp.handlers;

import com.microsoft.azure.gradle.webapp.DeployTask;
import com.microsoft.azure.gradle.webapp.configuration.AppService;
import com.microsoft.azure.gradle.webapp.configuration.AppServiceType;
import com.microsoft.azure.gradle.webapp.configuration.Deployment;
import com.microsoft.azure.gradle.webapp.configuration.DockerImageType;
import com.microsoft.azure.gradle.webapp.helpers.WebAppUtils;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

import static com.microsoft.azure.gradle.webapp.helpers.CommonStringTemplates.APP_SERVICE_PROPERTY_MISSING_TEMPLATE;
import static com.microsoft.azure.gradle.webapp.helpers.CommonStringTemplates.PROPERTY_MISSING_TEMPLATE;
import static com.microsoft.azure.gradle.webapp.helpers.CommonStringTemplates.UNKNOWN_VALUE_TEMPLATE;

public class HandlerFactoryImpl extends HandlerFactory {
    @Override
    public RuntimeHandler getRuntimeHandler(final DeployTask task) throws GradleException {
        AppService appService = task.getAzureWebAppExtension().getAppService();
        if (appService == null) {
            return new NullRuntimeHandlerImpl();
        }

        if (appService.getType() == AppServiceType.WINDOWS) {
            return new WindowsRuntimeHandlerImpl(task);
        }

        if (appService.getType() == AppServiceType.LINUX) {
            return new LinuxRuntimeHandlerImpl(task);
        }

        if (appService.getType() == AppServiceType.DOCKER) {
            final DockerImageType imageType = WebAppUtils.getDockerImageTypeFromName(appService);
            task.getLogger().quiet("imageType: " + imageType);
            switch (imageType) {
                case PUBLIC_DOCKER_HUB:
                    return new PublicDockerHubRuntimeHandlerImpl(task);
                case PRIVATE_DOCKER_HUB:
                    return new PrivateDockerHubRuntimeHandlerImpl(task);
                case PRIVATE_REGISTRY:
                    return new PrivateRegistryRuntimeHandlerImpl(task);
                case NONE:
                default:
                    throw new GradleException(
                            String.format(APP_SERVICE_PROPERTY_MISSING_TEMPLATE, "appService.imageName", "DOCKER"));
            }
        }
        throw new GradleException(String.format(UNKNOWN_VALUE_TEMPLATE, "appService.type"));
    }

    @Override
    public SettingsHandler getSettingsHandler(final Project project) throws GradleException {
        return new SettingsHandlerImpl(project);
    }

    @Override
    public ArtifactHandler getArtifactHandler(final DeployTask task) throws GradleException {
        Deployment deployment = task.getAzureWebAppExtension().getDeployment();
        if (deployment == null) {
            task.getLogger().quiet("No deployment configured, exit.");
            return null;
        }
        switch (deployment.getType()) {
            case NONE:
                throw new GradleException(String.format(PROPERTY_MISSING_TEMPLATE, "deployment.type"));
            case UNKNOWN:
                throw new GradleException(String.format(UNKNOWN_VALUE_TEMPLATE, "deployment.type"));
            case WAR:
                return new WarArtifactHandlerImpl(task);
            case ZIP:
                return new ZipArtifactHandlerImpl(task);
            case FTP:
            default:
                return new FTPArtifactHandlerImpl(task);
        }
    }
}
