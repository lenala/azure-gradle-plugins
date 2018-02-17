/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.gradle.functions;

import com.microsoft.azure.gradle.functions.auth.AuthConfiguration;
import com.microsoft.azure.gradle.functions.auth.AzureAuthFailureException;
import com.microsoft.azure.gradle.functions.auth.AzureAuthHelper;
import com.microsoft.azure.management.Azure;
import org.gradle.api.DefaultTask;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static com.microsoft.azure.gradle.functions.AzureFunctionsPlugin.AZURE_FUNCTIONS;

abstract class FunctionsTask extends DefaultTask implements AuthConfiguration {
    public static final String AZURE_INIT_FAIL = "Failed to authenticate with Azure. Please check your configuration.";

    protected AzureFunctionsExtension azureFunctionsExtension;
    protected AzureAuthHelper azureAuthHelper;
    private Azure azure;

    public String getFinalName() {
        return "finalName";
//        return finalName;
    }

    public Map getAppSettings() {
        return new HashMap();//appSettings;
    }

    @Override
    public String getUserAgent() {
        return getName() + " " + getGroup();
//        return String.format("%s/%s %s:%s %s:%s", this.getName(), this.getGroup()
//                getPluginName(), getPluginVersion(),
//                INSTALLATION_ID_KEY, getInstallationId(),
//                SESSION_ID_KEY, getSessionId());
    }

    @Override
    public String getSubscriptionId() {
        return (String) getProject().getProperties().get("subscriptionId");
    }

    @Override
    public boolean hasAuthenticationSettings() {
        return getProject().getProperties().containsKey(AzureAuthHelper.CLIENT_ID) || azureFunctionsExtension.getAuthFile() != null;
    }

    @Override
    public String getAuthenticationSetting(String key) {
        return (String) getProject().getProperties().get(key);
    }

    @Override
    public String getAuthFile() {
        return azureFunctionsExtension.getAuthFile();
    }

    public Azure getAzureClient() throws AzureAuthFailureException {
        if (azure == null) {
            azure = azureAuthHelper.getAzureClient();
            if (azure == null) {
//                getTelemetryProxy().trackEvent(INIT_FAILURE);
                throw new AzureAuthFailureException(AZURE_INIT_FAIL);
            } else {
                // Repopulate subscriptionId in case it is not configured.
//                getTelemetryProxy().addDefaultProperty(SUBSCRIPTION_ID_KEY, azure.subscriptionId());
            }
        }
        return azure;
    }

    public String getBuildDirectoryAbsolutePath() {
        return getProject().getBuildDir().getAbsolutePath();
    }

    public String getDeploymentStageDirectory() {
        return Paths.get(getBuildDirectoryAbsolutePath(),
                AZURE_FUNCTIONS,
                azureFunctionsExtension.getAppName()).toString();
    }
}
