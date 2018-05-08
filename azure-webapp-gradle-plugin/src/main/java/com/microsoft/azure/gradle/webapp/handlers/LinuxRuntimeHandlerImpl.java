/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.gradle.webapp.handlers;

import com.microsoft.azure.gradle.webapp.AzureWebAppExtension;
import com.microsoft.azure.gradle.webapp.DeployTask;
import com.microsoft.azure.gradle.webapp.helpers.WebAppUtils;
import com.microsoft.azure.management.appservice.RuntimeStack;
import com.microsoft.azure.management.appservice.WebApp;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.GradleException;

public class LinuxRuntimeHandlerImpl implements RuntimeHandler {

    private static final String NOT_SUPPORTED_IMAGE = "The image: '%s' is not supported.";
    private static final String IMAGE_NOT_GIVEN = "Image name is not specified.";

    private DeployTask task;
    private AzureWebAppExtension extension;

    public LinuxRuntimeHandlerImpl(final DeployTask task) {
        this.task = task;
        this.extension = task.getAzureWebAppExtension();
    }

    @Override
    public WebApp.DefinitionStages.WithCreate defineAppWithRuntime() throws Exception {
        return WebAppUtils.defineApp(task)
                .withNewLinuxPlan(extension.getPricingTier())
                .withBuiltInImage(this.getJavaRunTimeStack(extension.getAppServiceOnLinux().getRuntimeStack()));
    }

    @Override
    public WebApp.Update updateAppRuntime(WebApp app) throws Exception {
        WebAppUtils.assureLinuxWebApp(app);
        return app.update().withBuiltInImage(this.getJavaRunTimeStack(extension.getContainerSettings().getImageName()));
    }

    private RuntimeStack getJavaRunTimeStack(String imageName) throws Exception {
        if (StringUtils.isNotEmpty(imageName)) {
            if (imageName.equalsIgnoreCase(RuntimeStack.TOMCAT_8_5_JRE8.toString())) {
                return RuntimeStack.TOMCAT_8_5_JRE8;
            } else if (imageName.equalsIgnoreCase(RuntimeStack.TOMCAT_9_0_JRE8.toString())) {
                return RuntimeStack.TOMCAT_9_0_JRE8;
            } else {
                throw new GradleException(String.format(NOT_SUPPORTED_IMAGE, imageName));
            }
        }
        throw new GradleException(IMAGE_NOT_GIVEN);
    }
}
