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
import com.microsoft.azure.management.appservice.JavaVersion;
import com.microsoft.azure.management.appservice.PricingTier;
import com.microsoft.azure.management.appservice.WebContainer;
import groovy.lang.Closure;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Project;
import org.gradle.api.tasks.Input;

public class AzureWebAppExtension {
    public static final String WEBAPP_EXTENSION_NAME = "azurewebapp";
    @Input
    private String appName;
    @Input
    private String resourceGroup;
    @Input
    String region = "westus2";
    @Input
    private PricingTierEnum pricingTier;
    @Input
    private String target;
    @Input
    private boolean stopAppDuringDeployment;
    @Input
    private String subscriptionId;
    @Input
    private String authFile;
    @Input
    private DeploymentType deploymentType = DeploymentType.FTP;

    private AppServiceOnLinux appServiceOnLinux;

    private AppServiceOnWindows appServiceOnWindows;

    private ContainerSettings containerSettings;

    public void setContainerSettings(Closure closure) {
        containerSettings = new ContainerSettings();
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.setDelegate(containerSettings);
        closure.run();
    }

    public void setAppServiceOnWindows(Closure closure) {
        appServiceOnWindows = new AppServiceOnWindows();
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.setDelegate(appServiceOnWindows);
        closure.run();
    }

    public void setAppServiceOnLinux(Closure closure) {
        appServiceOnLinux = new AppServiceOnLinux();
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.setDelegate(appServiceOnLinux);
        closure.run();
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

    public String getAuthFile() {
        return authFile;
    }

    public DeploymentType getDeploymentType() {
        return deploymentType;
    }
}
