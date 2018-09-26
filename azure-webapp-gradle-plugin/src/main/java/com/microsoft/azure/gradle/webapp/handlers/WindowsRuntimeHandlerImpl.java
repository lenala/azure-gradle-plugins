/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.gradle.webapp.handlers;

import com.microsoft.azure.gradle.webapp.AzureWebAppExtension;
import com.microsoft.azure.gradle.webapp.DeployTask;
import com.microsoft.azure.gradle.webapp.helpers.WebAppUtils;
import com.microsoft.azure.management.appservice.AppServicePlan;
import com.microsoft.azure.management.appservice.OperatingSystem;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.appservice.WebApp.DefinitionStages.WithCreate;
import com.microsoft.azure.management.appservice.WebApp.Update;

public class WindowsRuntimeHandlerImpl implements RuntimeHandler {
    private DeployTask task;
    private AzureWebAppExtension extension;

    public WindowsRuntimeHandlerImpl(final DeployTask task) {
        this.task = task;
        this.extension = task.getAzureWebAppExtension();
    }

    @Override
    public WithCreate defineAppWithRuntime() throws Exception {
        final AppServicePlan plan = WebAppUtils.createOrGetAppServicePlan(task, OperatingSystem.WINDOWS);
        final WithCreate withCreate = WebAppUtils.defineWindowsApp(task, plan);

        withCreate.withJavaVersion(extension.getAppService().getJavaVersion())
                .withWebContainer(extension.getAppService().getJavaWebContainer());
        return withCreate;
    }

    @Override
    public Update updateAppRuntime(final WebApp app) {
        WebAppUtils.assureWindowsWebApp(app);

        final Update update = app.update();
        update.withJavaVersion(extension.getAppService().getJavaVersion())
                .withWebContainer(extension.getAppService().getJavaWebContainer());
        return update;
    }
}
