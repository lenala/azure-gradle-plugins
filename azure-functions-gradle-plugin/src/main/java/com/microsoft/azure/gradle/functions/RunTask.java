/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.gradle.functions;

import com.microsoft.azure.gradle.functions.auth.AuthConfiguration;
import com.microsoft.azure.gradle.functions.auth.AzureAuthHelper;
import com.microsoft.azure.management.Azure;
import org.gradle.api.DefaultTask;

public class RunTask  extends DefaultTask implements AuthConfiguration {
    public static final String LOAD_TEMPLATES = "Step 1 of 4: Load all function templates";
    public static final String LOAD_TEMPLATES_DONE = "Successfully loaded all function templates";
    public static final String LOAD_TEMPLATES_FAIL = "Failed to load all function templates.";
    public static final String FIND_TEMPLATE = "Step 2 of 4: Select function template";
    public static final String FIND_TEMPLATE_DONE = "Successfully found function template: ";
    public static final String FIND_TEMPLATE_FAIL = "Function template not found: ";
    public static final String PREPARE_PARAMS = "Step 3 of 4: Prepare required parameters";
    public static final String FOUND_VALID_VALUE = "Found valid value. Skip user input.";
    public static final String SAVE_FILE = "Step 4 of 4: Saving function to file";
    public static final String SAVE_FILE_DONE = "Successfully saved new function at ";
    public static final String FILE_EXIST = "Function already exists at %s. Please specify a different function name.";
    private static final String FUNCTION_NAME_REGEXP = "^[a-zA-Z][a-zA-Z\\d_\\-]*$";

    private Azure azure;
    private AzureFunctionsExtension azureFunctionsExtension;
    private AzureAuthHelper azureAuthHelper;

    public void setAzureFunctionsExtension(AzureFunctionsExtension azureFunctionsExtension) {
        this.azureFunctionsExtension = azureFunctionsExtension;
        azureAuthHelper = new AzureAuthHelper(this);
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

}
