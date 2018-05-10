/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.gradle.webapp.helpers;

import com.microsoft.azure.gradle.webapp.AzureWebAppExtension;
import com.microsoft.azure.gradle.webapp.DeployTask;
import com.microsoft.azure.gradle.webapp.configuration.ContainerSettings;
import com.microsoft.azure.gradle.webapp.configuration.DockerImageType;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.AppServicePlan;
import com.microsoft.azure.management.appservice.OperatingSystem;
import com.microsoft.azure.management.appservice.RuntimeStack;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.appservice.WebApp.DefinitionStages.WithNewAppServicePlan;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource.DefinitionStages.WithGroup;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskExecutionException;

import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class WebAppUtils {
    private static final String CONTAINER_SETTING_NOT_APPLICABLE =
            "Trying to deploy to existing Web App on Windows; 'containerSettings' or 'appServiceOnLinux' not applicaple. " +
                    "Please use 'appServiceOnWindows' to configure your runtime.";
    private static final String JAVA_VERSION_NOT_APPLICABLE = "Trying to deploy to existing Web App on Linux; 'appServiceOnWindows' is not applicable to Web App on Linux. " +
            "please use 'containerSettings' or appServiceOnWindows' to specify your runtime.";
    private static final String NOT_SUPPORTED_IMAGE = "The image: '%s' is not supported.";
    private static final String IMAGE_NOT_GIVEN = "Image name is not specified.";
    private static final String SERVICE_PLAN_NOT_APPLICABLE = "The App Service Plan '%s' is not a %s Plan";

    private static final String CREATE_SERVICE_PLAN = "Creating App Service Plan '%s'...";
    private static final String SERVICE_PLAN_EXIST = "Found existing App Service Plan '%s' in Resource Group '%s'.";
    private static final String SERVICE_PLAN_CREATED = "Successfully created App Service Plan.";

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

    public static WebApp.DefinitionStages.WithDockerContainerImage defineLinuxApp(DeployTask task, final AppServicePlan plan)
            throws Exception {
        assureLinuxPlan(plan);

        final String resourceGroup = task.getAzureWebAppExtension().getResourceGroup();
        final WebApp.DefinitionStages.ExistingLinuxPlanWithGroup existingLinuxPlanWithGroup = task.getAzureClient().webApps()
                .define(task.getAzureWebAppExtension().getAppName())
                .withExistingLinuxPlan(plan);
        return task.getAzureClient().resourceGroups().contain(resourceGroup) ?
                existingLinuxPlanWithGroup.withExistingResourceGroup(resourceGroup) :
                existingLinuxPlanWithGroup.withNewResourceGroup(resourceGroup);
    }

    private static void assureLinuxPlan(final AppServicePlan plan) throws GradleException {
        if (!plan.operatingSystem().equals(OperatingSystem.LINUX)) {
            throw new GradleException(String.format(SERVICE_PLAN_NOT_APPLICABLE,
                    plan.name(), OperatingSystem.LINUX.name()));
        }
    }

    public static WebApp.DefinitionStages.WithCreate defineWindowsApp(DeployTask task, final AppServicePlan plan)
            throws Exception {
        assureWindowsPlan(plan);

        final String resourceGroup = task.getAzureWebAppExtension().getResourceGroup();
        final WebApp.DefinitionStages.ExistingWindowsPlanWithGroup existingWindowsPlanWithGroup =  task.getAzureClient().webApps()
                .define(task.getAzureWebAppExtension().getAppName())
                .withExistingWindowsPlan(plan);
        return task.getAzureClient().resourceGroups().contain(resourceGroup) ?
                existingWindowsPlanWithGroup.withExistingResourceGroup(resourceGroup) :
                existingWindowsPlanWithGroup.withNewResourceGroup(resourceGroup);
    }

    private static void assureWindowsPlan(final AppServicePlan plan) throws GradleException {
        if (!plan.operatingSystem().equals(OperatingSystem.WINDOWS)) {
            throw new GradleException(String.format(SERVICE_PLAN_NOT_APPLICABLE,
                    plan.name(), OperatingSystem.WINDOWS.name()));
        }
    }

    public static AppServicePlan createOrGetAppServicePlan(DeployTask task, OperatingSystem os)
            throws Exception {
        AzureWebAppExtension extension = task.getAzureWebAppExtension();
        AppServicePlan plan = null;
        final String servicePlanResGrp = StringUtils.isNotEmpty(extension.getAppServicePlanResourceGroup()) ?
                extension.getAppServicePlanResourceGroup() : extension.getResourceGroup();

        String servicePlanName = extension.getAppServicePlanName();
        if (StringUtils.isNotEmpty(servicePlanName)) {
            plan = task.getAzureClient().appServices().appServicePlans()
                    .getByResourceGroup(servicePlanResGrp, servicePlanName);
        } else {
            servicePlanName = SdkContext.randomResourceName("ServicePlan", 18);
        }

        final Azure azure = task.getAzureClient();
        if (plan == null) {
            task.getLogger().quiet(String.format(CREATE_SERVICE_PLAN, servicePlanName));

            final AppServicePlan.DefinitionStages.WithGroup withGroup = azure.appServices().appServicePlans()
                    .define(servicePlanName)
                    .withRegion(extension.getRegion());

            final AppServicePlan.DefinitionStages.WithPricingTier withPricingTier
                    = azure.resourceGroups().contain(servicePlanResGrp) ?
                    withGroup.withExistingResourceGroup(servicePlanResGrp) :
                    withGroup.withNewResourceGroup(servicePlanResGrp);

            plan = withPricingTier.withPricingTier(extension.getPricingTier())
                    .withOperatingSystem(os).create();

            task.getLogger().quiet(SERVICE_PLAN_CREATED);
        } else {
            task.getLogger().quiet(String.format(SERVICE_PLAN_EXIST, servicePlanName, servicePlanResGrp));
        }

        return plan;
    }

    public static DockerImageType getDockerImageType(final ContainerSettings containerSettings) {
        if (containerSettings == null || StringUtils.isEmpty(containerSettings.getImageName())) {
            return DockerImageType.NONE;
        }
        final boolean isCustomRegistry = isNotEmpty(containerSettings.getRegistryUrl());
        final boolean isPrivate = isNotEmpty(containerSettings.getServerId());

        Logging.getLogger(WebAppUtils.class).quiet("ServerId: " + containerSettings.getServerId() + " : " + System.getenv("SERVER_ID"));

        if (isCustomRegistry) {
            return isPrivate ? DockerImageType.PRIVATE_REGISTRY : DockerImageType.UNKNOWN;
        } else {
            return isPrivate ? DockerImageType.PRIVATE_DOCKER_HUB : DockerImageType.PUBLIC_DOCKER_HUB;
        }
    }

    public static RuntimeStack getLinuxRunTimeStack(String imageName) throws GradleException {
        if (isNotEmpty(imageName)) {
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
