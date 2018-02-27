/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.gradle.functions;

import com.microsoft.azure.gradle.functions.handlers.ArtifactHandler;
import com.microsoft.azure.gradle.functions.handlers.FTPArtifactHandlerImpl;
import com.microsoft.azure.gradle.functions.handlers.MSDeployArtifactHandlerImpl;
import com.microsoft.azure.gradle.functions.model.PricingTierEnum;
import com.microsoft.azure.management.appservice.FunctionApp;
import com.microsoft.azure.management.appservice.PricingTier;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class DeployTask extends FunctionsTask {
    private static final String FUNCTION_DEPLOY_START = "Starting deploying to Function App ";
    private static final String FUNCTION_DEPLOY_SUCCESS =
            "Successfully deployed Function App at https://%s.azurewebsites.net";
    private static final String FUNCTION_APP_CREATE_START = "Target Function App does not exist. " +
            "Creating a new Function App ...";
    private static final String FUNCTION_APP_CREATED = "Successfully created Function App ";
    private static final String FUNCTION_APP_UPDATE = "Updating Function App...";
    private static final String FUNCTION_APP_UPDATE_DONE = "Successfully updated Function App ";

    private static final String MS_DEPLOY = "msdeploy";
    private static final String FTP = "ftp";

    /**
     * Function App pricing tier, which will only be used to create Function App at the first time.<br/>
     * Below is the list of supported pricing tier. If left blank, Consumption plan is the default.
     * <ul>
     * <li>F1</li>
     * <li>D1</li>
     * <li>B1</li>
     * <li>B2</li>
     * <li>B3</li>
     * <li>S1</li>
     * <li>S2</li>
     * <li>S3</li>
     * <li>P1</li>
     * <li>P2</li>
     * <li>P3</li>
     * </ul>
     */
    private PricingTierEnum pricingTier;

    /**
     * Deployment type to deploy Web App. Supported values:
     * <ul>
     * <li>msdeploy</li>
     * <li>ftp</li>
     * </ul>
     *
     * @since 0.1.0
     */
    private String deploymentType;

    public void setPricingTier(PricingTierEnum pricingTier) {
        this.pricingTier = pricingTier;
    }

    public void setDeploymentType(String deploymentType) {
        this.deploymentType = deploymentType;
    }

    public PricingTier getPricingTier() {
        return pricingTier == null ? null : pricingTier.toPricingTier();
    }

    public String getDeploymentType() {
        return StringUtils.isEmpty(deploymentType) ? MS_DEPLOY : deploymentType;
    }


    @TaskAction
    void deployFunction() {
        try {
            getLogger().quiet(FUNCTION_DEPLOY_START + getAppName() + "...");

            createOrUpdateFunctionApp();

            getArtifactHandler().publish();

            getLogger().quiet(String.format(FUNCTION_DEPLOY_SUCCESS, getAppName()));
        } catch (Exception ex) {
            throw new TaskExecutionException(this, ex);
        }
    }

    private void createOrUpdateFunctionApp() throws Exception {
        final FunctionApp app = getFunctionApp();
        if (app == null) {
            createFunctionApp();
        } else {
            updateFunctionApp(app);
        }
    }

    private void createFunctionApp() throws Exception {
        getLogger().quiet(FUNCTION_APP_CREATE_START);

        final FunctionApp.DefinitionStages.NewAppServicePlanWithGroup newAppServicePlanWithGroup = defineApp(getAppName(), getRegion());
        final FunctionApp.DefinitionStages.WithCreate withCreate = configureResourceGroup(newAppServicePlanWithGroup, getResourceGroup());
        configurePricingTier(withCreate, getPricingTier());
        configureAppSettings(withCreate::withAppSettings, getAppSettings());
        withCreate.create();

        getLogger().quiet(FUNCTION_APP_CREATED + getAppName());
    }

    private void updateFunctionApp(final FunctionApp app) {
        getLogger().quiet(FUNCTION_APP_UPDATE);

        // Work around of https://github.com/Azure/azure-sdk-for-java/issues/1755
        app.inner().withTags(null);

        final FunctionApp.Update update = app.update();
        configureAppSettings(update::withAppSettings, getAppSettings());
        update.apply();

        getLogger().quiet(FUNCTION_APP_UPDATE_DONE + getAppName());
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

    private ArtifactHandler getArtifactHandler() {
        switch (getDeploymentType().toLowerCase(Locale.ENGLISH)) {
            case FTP:
                return new FTPArtifactHandlerImpl(this);
            case MS_DEPLOY:
            default:
                return new MSDeployArtifactHandlerImpl(this);
        }
    }
}
