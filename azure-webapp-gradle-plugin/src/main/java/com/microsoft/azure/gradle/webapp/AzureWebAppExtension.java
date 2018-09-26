/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.gradle.webapp;

import com.microsoft.azure.gradle.webapp.configuration.AppService;
import com.microsoft.azure.gradle.webapp.configuration.Authentication;
import com.microsoft.azure.gradle.webapp.configuration.Deployment;
import com.microsoft.azure.gradle.webapp.model.PricingTierEnum;
import com.microsoft.azure.management.appservice.PricingTier;
import groovy.lang.Closure;
import org.gradle.api.Project;

public class AzureWebAppExtension {
    public static final String WEBAPP_EXTENSION_NAME = "azureWebApp";
    private final Project project;
    private String subscriptionId = "";
    private String appName;
    private String resourceGroup;
    private String region = "westus2";
    private String appServicePlanResourceGroup;
    private String appServicePlanName;
    private PricingTierEnum pricingTier;
    private boolean stopAppDuringDeployment;
    private AppService appService;
    private Authentication authentication;
    private Deployment deployment;

    public AzureWebAppExtension(Project project) {
        this.project = project;
    }

    public AppService getAppService() {
        return appService;
    }

    public void setAppService(Closure closure) {
        appService = new AppService();
        project.configure(appService, closure);
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

    public boolean isStopAppDuringDeployment() {
        return stopAppDuringDeployment;
    }

    public String getAppServicePlanResourceGroup() {
        return appServicePlanResourceGroup;
    }

    public String getAppServicePlanName() {
        return appServicePlanName;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Closure closure) {
        authentication = new Authentication();
        project.configure(authentication, closure);
    }

    public Deployment getDeployment() {
        return deployment;
    }

    public void setDeployment(Closure closure) {
        deployment = new Deployment();
        project.configure(deployment, closure);
    }
}
