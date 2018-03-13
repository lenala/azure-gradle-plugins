/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.gradle.webapp;

import com.microsoft.azure.gradle.webapp.configuration.ContainerSettings;
import com.microsoft.azure.gradle.webapp.model.PricingTierEnum;
import com.microsoft.azure.management.appservice.JavaVersion;
import com.microsoft.azure.management.appservice.PricingTier;
import com.microsoft.azure.management.appservice.WebContainer;
import groovy.lang.Closure;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Project;
import org.gradle.api.tasks.Input;

public class AzureWebAppExtension {
    @Input
    private String appName;
    @Input
    private String resourceGroup;
    @Input
    String region = "westus2";
    @Input
    private String javaVersion;
    @Input
    private PricingTierEnum pricingTier;
    @Input
    private String javaWebContainer;
    @Input
    private String target;
    @Input
    String packageUri;
    @Input
    private boolean stopAppDuringDeployment;
    @Input
    private String subscriptionId;
    @Input
    private String authFile;

    ContainerSettings containerSettings;
    private Project project;

    public AzureWebAppExtension(Project project) {
        this.project = project;
    }

    public void setContainerSettings(Closure closure) {
        containerSettings = new ContainerSettings();
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.setDelegate(containerSettings);
        closure.run();
    }

    public ContainerSettings getContainerSettings() {
        return containerSettings;
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

    public JavaVersion getJavaVersion() {
        return StringUtils.isEmpty(javaVersion) ? null : JavaVersion.fromString(javaVersion);
    }

    public WebContainer getJavaWebContainer() {
        return StringUtils.isEmpty(javaWebContainer)
                ? WebContainer.TOMCAT_8_5_NEWEST
                : WebContainer.fromString(javaWebContainer);
    }

    public String getTarget() {
        return target;
    }

    public String getPackageUri() {
        return packageUri;
    }

    public boolean isStopAppDuringDeployment() {
        return stopAppDuringDeployment;
    }

    public String getAuthFile() {
        return authFile;
    }
}
