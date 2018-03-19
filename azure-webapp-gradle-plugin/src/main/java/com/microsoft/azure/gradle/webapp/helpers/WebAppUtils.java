/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.gradle.webapp.helpers;

import com.microsoft.azure.gradle.webapp.DeployTask;
import com.microsoft.azure.gradle.webapp.configuration.ContainerSettings;
import com.microsoft.azure.gradle.webapp.configuration.DockerImageType;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.appservice.WebApp.DefinitionStages.WithNewAppServicePlan;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource.DefinitionStages.WithGroup;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskExecutionException;

public class WebAppUtils {
    public static final String CONTAINER_SETTING_NOT_APPLICABLE =
            "<containerSettings> is not applicable to Web App on Windows; " +
                    "please use <javaVersion> and <javaWebContainer> to configure your runtime.";
    public static final String JAVA_VERSION_NOT_APPLICABLE = "<javaVersion> is not applicable to Web App on Linux; " +
            "please use <containerSettings> to specify your runtime.";

    private static boolean isLinuxWebApp(final WebApp app) {
        return app.inner().kind().contains("linux");
    }

    public static void assureLinuxWebApp(final WebApp app) throws GradleException {
        if (!isLinuxWebApp(app)) {
            throw new GradleException(CONTAINER_SETTING_NOT_APPLICABLE);
        }
    }

    public static void assureWindowsWebApp(final WebApp app) throws TaskExecutionException {
        if (isLinuxWebApp(app)) {
            throw new GradleException(JAVA_VERSION_NOT_APPLICABLE);
        }
    }

    public static WithNewAppServicePlan defineApp(final DeployTask task) throws Exception {
        final WithGroup<WithNewAppServicePlan> withGroup = task.getAzureClient().webApps()
                .define(task.getAzureWebAppExtension().getAppName())
                .withRegion(task.getAzureWebAppExtension().getRegion());
        final String resourceGroup = task.getAzureWebAppExtension().getResourceGroup();
        return task.getAzureClient().resourceGroups().checkExistence(resourceGroup) ?
                withGroup.withExistingResourceGroup(resourceGroup) :
                withGroup.withNewResourceGroup(resourceGroup);
    }

    public static DockerImageType getDockerImageType(final ContainerSettings containerSettings) {
        if (containerSettings == null || StringUtils.isEmpty(containerSettings.getImageName())) {
            return DockerImageType.NONE;
        }
        final boolean isCustomRegistry = StringUtils.isNotEmpty(containerSettings.getRegistryUrl());
        final boolean isPrivate = StringUtils.isNotEmpty(containerSettings.getServerId());

        if (isCustomRegistry) {
            return isPrivate ? DockerImageType.PRIVATE_REGISTRY : DockerImageType.UNKNOWN;
        } else {
            return isPrivate ? DockerImageType.PRIVATE_DOCKER_HUB : DockerImageType.PUBLIC_DOCKER_HUB;
        }
    }

    /**
     * Work Around:
     * When a web app is created from Azure Portal, there are hidden tags associated with the app.
     * It will be messed up when calling "update" API.
     * An issue is logged at https://github.com/Azure/azure-sdk-for-java/issues/1755 .
     * Remove all tags here to make it work.
     *
     * @param app
     */
    public static void clearTags(final WebApp app) {
        app.inner().withTags(null);
    }
}
