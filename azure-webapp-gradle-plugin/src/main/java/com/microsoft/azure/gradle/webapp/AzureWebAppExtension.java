/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.gradle.webapp;

import com.microsoft.azure.gradle.webapp.configuration.AppServiceOnLinux;
import com.microsoft.azure.gradle.webapp.configuration.AppServiceOnWindows;
import com.microsoft.azure.gradle.webapp.configuration.ContainerSettings;
import com.microsoft.azure.gradle.webapp.configuration.DeploymentType;
import com.microsoft.azure.gradle.webapp.model.PricingTierEnum;
import com.microsoft.azure.management.appservice.PricingTier;
import groovy.lang.Closure;
import org.gradle.api.Project;
import org.gradle.api.tasks.Input;

import java.io.File;

public class AzureWebAppExtension {
    public static final String WEBAPP_EXTENSION_NAME = "azurewebapp";
    private final Project project;
    @Input
    private String appName;
    @Input
    private String resourceGroup;
    @Input
    private String region = "westus2";
    @Input
    private String appServicePlanResourceGroup;
    @Input
    private String appServicePlanName;
    @Input
    private PricingTierEnum pricingTier;
    @Input
    private String target;
    @Input
    private boolean stopAppDuringDeployment;
    @Input
    private File authFile;
    @Input
    private DeploymentType deploymentType = DeploymentType.WARDEPLOY;

    private AppServiceOnLinux appServiceOnLinux;

    private AppServiceOnWindows appServiceOnWindows;

    private ContainerSettings containerSettings;

    public AzureWebAppExtension(Project project) {
        this.project = project;
    }

    public void setContainerSettings(Closure closure) {
        containerSettings = new ContainerSettings();
        project.configure(containerSettings, closure);
    }

    public void setAppServiceOnWindows(Closure closure) {
        appServiceOnWindows = new AppServiceOnWindows();
        project.configure(appServiceOnWindows, closure);
    }

    public void setAppServiceOnLinux(Closure closure) {
        appServiceOnLinux = new AppServiceOnLinux();
        project.configure(appServiceOnLinux, closure);
    }

    public ContainerSettings getContainerSettings() {
        return containerSettings;
    }

    public AppServiceOnLinux getAppServiceOnLinux() {
        return appServiceOnLinux;
    }

    public AppServiceOnWindows getAppServiceOnWindows() {
        return appServiceOnWindows;
    }

    public String getAppName() {
        return appName;
    }

    public String getResourceGroup() {
        return resourceGroup;
    }

    public String getRegion() {
        return region;
    }

    public PricingTier getPricingTier() {
        return pricingTier == null ? PricingTier.STANDARD_S1 : pricingTier.toPricingTier();
    }

    public String getTarget() {
        return target;
    }

    public boolean isStopAppDuringDeployment() {
        return stopAppDuringDeployment;
    }

    public File getAuthFile() {
        return authFile;
    }

    public DeploymentType getDeploymentType() {
        return deploymentType;
    }

    public String getAppServicePlanResourceGroup() {
        return appServicePlanResourceGroup;
    }

    public String getAppServicePlanName() {
        return appServicePlanName;
    }
}
