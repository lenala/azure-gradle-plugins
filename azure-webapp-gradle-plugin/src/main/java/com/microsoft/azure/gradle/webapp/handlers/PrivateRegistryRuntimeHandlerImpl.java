/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.gradle.webapp.handlers;

import com.microsoft.azure.gradle.webapp.AzureWebAppExtension;
import com.microsoft.azure.gradle.webapp.DeployTask;
import com.microsoft.azure.gradle.webapp.helpers.WebAppUtils;
import com.microsoft.azure.gradle.webapp.configuration.ContainerSettings;
import com.microsoft.azure.gradle.webapp.helpers.Utils;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.appservice.WebApp.DefinitionStages.WithCreate;
import com.microsoft.azure.management.appservice.WebApp.Update;
import org.gradle.api.GradleException;
import com.microsoft.azure.gradle.webapp.configuration.Server;

public class PrivateRegistryRuntimeHandlerImpl implements RuntimeHandler {
    public static final String SERVER_ID_NOT_FOUND = "Server Id not found in gradle.properties. ServerId=";

    private DeployTask task;
    private AzureWebAppExtension extension;

    public PrivateRegistryRuntimeHandlerImpl(final DeployTask task) {
        this.task = task;
        this.extension = task.getAzureWebAppExtension();
    }

    @Override
    public WithCreate defineAppWithRuntime() throws Exception {
        final ContainerSettings containerSetting = extension.getContainerSettings();
        final Server server = Utils.getServer(task.getProject(), containerSetting.getServerId());
        if (server == null) {
            throw new GradleException(SERVER_ID_NOT_FOUND + containerSetting.getServerId());
        }
        return WebAppUtils.defineApp(task)
                .withNewLinuxPlan(extension.getPricingTier())
                .withPrivateRegistryImage(containerSetting.getImageName(), containerSetting.getRegistryUrl())
                .withCredentials(server.getUsername(), server.getPassword());
    }

    @Override
    public Update updateAppRuntime(final WebApp app) throws Exception {
        WebAppUtils.assureLinuxWebApp(app);
        WebAppUtils.clearTags(app);
        final ContainerSettings containerSettings = extension.getContainerSettings();
        final Server server = Utils.getServer(task.getProject(), containerSettings.getServerId());
        if (server == null) {
            throw new GradleException(SERVER_ID_NOT_FOUND + containerSettings.getServerId());
        }
        return app.update()
                .withPrivateRegistryImage(containerSettings.getImageName(), containerSettings.getRegistryUrl())
                .withCredentials(server.getUsername(), server.getPassword());
    }
}
