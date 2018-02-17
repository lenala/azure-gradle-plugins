/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.gradle.functions;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.microsoft.azure.gradle.functions.auth.AuthConfiguration;
import com.microsoft.azure.gradle.functions.auth.AzureAuthHelper;
import com.microsoft.azure.gradle.functions.configuration.FunctionConfiguration;
import com.microsoft.azure.gradle.functions.handlers.AnnotationHandler;
import com.microsoft.azure.gradle.functions.handlers.ArtifactHandler;
import com.microsoft.azure.gradle.functions.handlers.FTPArtifactHandlerImpl;
import com.microsoft.azure.gradle.functions.handlers.MSDeployArtifactHandlerImpl;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.FunctionApp;
import com.microsoft.azure.management.appservice.PricingTier;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class DeployTask extends FunctionsTask {
    public static final String FUNCTION_DEPLOY_START = "Starting deploying to Function App ";
    public static final String FUNCTION_DEPLOY_SUCCESS =
            "Successfully deployed Function App at https://%s.azurewebsites.net";
    public static final String FUNCTION_APP_CREATE_START = "Target Function App does not exist. " +
            "Creating a new Function App ...";
    public static final String FUNCTION_APP_CREATED = "Successfully created Function App ";
    public static final String FUNCTION_APP_UPDATE = "Updating Function App...";
    public static final String FUNCTION_APP_UPDATE_DONE = "Successfully updated Function App ";

    public static final String MS_DEPLOY = "msdeploy";
    public static final String FTP = "ftp";

    private Azure azure;
    private AzureFunctionsExtension azureFunctionsExtension;
    private AzureAuthHelper azureAuthHelper;

    public void setAzureFunctionsExtension(AzureFunctionsExtension azureFunctionsExtension) {
        this.azureFunctionsExtension = azureFunctionsExtension;
        azureAuthHelper = new AzureAuthHelper(this);
    }

    @TaskAction
    void packageFunction() {
        try {
            getLogger().quiet(FUNCTION_DEPLOY_START + azureFunctionsExtension.getAppName() + "...");

            createOrUpdateFunctionApp();

            getArtifactHandler().publish();

            getLogger().quiet(String.format(FUNCTION_DEPLOY_SUCCESS, azureFunctionsExtension.getAppName()));
        } catch (Exception ex) {
            throw new TaskExecutionException(this, ex);
        }
    }

    protected void createOrUpdateFunctionApp() throws Exception {
        final FunctionApp app = getFunctionApp();
        if (app == null) {
            createFunctionApp();
        } else {
            updateFunctionApp(app);
        }
    }

    protected void createFunctionApp() throws Exception {
        getLogger().quiet(FUNCTION_APP_CREATE_START);

        final FunctionApp.DefinitionStages.NewAppServicePlanWithGroup newAppServicePlanWithGroup = defineApp(getAppName(), getRegion());
        final FunctionApp.DefinitionStages.WithCreate withCreate = configureResourceGroup(newAppServicePlanWithGroup, getResourceGroup());
        configurePricingTier(withCreate, getPricingTier());
        configureAppSettings(withCreate::withAppSettings, getAppSettings());
        withCreate.create();

        getLogger().quiet(FUNCTION_APP_CREATED + getAppName());
    }

    protected void updateFunctionApp(final FunctionApp app) {
        getLogger().quiet(FUNCTION_APP_UPDATE);

        // Work around of https://github.com/Azure/azure-sdk-for-java/issues/1755
        app.inner().withTags(null);

        final FunctionApp.Update update = app.update();
        configureAppSettings(update::withAppSettings, getAppSettings());
        update.apply();

        getLogger().quiet(FUNCTION_APP_UPDATE_DONE + azureFunctionsExtension.getAppName());
    }

    private FunctionApp.DefinitionStages.NewAppServicePlanWithGroup defineApp(final String appName, final String region) throws Exception {
        return getAzureClient().appServices().functionApps().define(appName).withRegion(region);
    }

    private FunctionApp.DefinitionStages.WithCreate configureResourceGroup(final FunctionApp.DefinitionStages.NewAppServicePlanWithGroup newAppServicePlanWithGroup,
                                                                           final String resourceGroup) throws Exception {
        return isResourceGroupExist(resourceGroup) ?
                newAppServicePlanWithGroup.withExistingResourceGroup(resourceGroup) :
                newAppServicePlanWithGroup.withNewResourceGroup(resourceGroup);
    }

    private boolean isResourceGroupExist(final String resourceGroup) throws Exception {
        return getAzureClient().resourceGroups().checkExistence(resourceGroup);
    }

    private void configurePricingTier(final FunctionApp.DefinitionStages.WithCreate withCreate, final PricingTier pricingTier) {
        if (pricingTier != null) {
            // Enable Always On when using app service plan
            withCreate.withNewAppServicePlan(pricingTier).withWebAppAlwaysOn(true);
        } else {
            withCreate.withNewConsumptionPlan();
        }
    }

    private void configureAppSettings(final Consumer<Map> withAppSettings, final Map appSettings) {
        if (appSettings != null && !appSettings.isEmpty()) {
            withAppSettings.accept(appSettings);
        }
    }


    protected ArtifactHandler getArtifactHandler() {
        switch (getDeploymentType().toLowerCase(Locale.ENGLISH)) {
            case FTP:
                return new FTPArtifactHandlerImpl(this);
            case MS_DEPLOY:
            default:
                return new MSDeployArtifactHandlerImpl(this);
        }
    }




}
