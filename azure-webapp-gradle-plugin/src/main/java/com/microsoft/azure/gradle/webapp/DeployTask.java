/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.gradle.webapp;

import com.microsoft.azure.gradle.webapp.auth.AuthConfiguration;
import com.microsoft.azure.gradle.webapp.auth.AuthenticationSetting;
import com.microsoft.azure.gradle.webapp.auth.AzureAuthFailureException;
import com.microsoft.azure.gradle.webapp.auth.AzureAuthHelper;
import com.microsoft.azure.gradle.webapp.handlers.HandlerFactory;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.WebApp;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import java.io.File;

public class DeployTask extends DefaultTask implements AuthConfiguration {
    public static final String AZURE_INIT_FAIL = "Failed to authenticate with Azure. Please check your configuration.";

    public static final String WEBAPP_DEPLOY_START = "Start deploying to Web App %s...";
    public static final String WEBAPP_DEPLOY_SUCCESS = "Successfully deployed Web App at https://%s.azurewebsites.net";
    public static final String WEBAPP_NOT_EXIST = "Target Web App doesn't exist. Creating a new one...";
    public static final String WEBAPP_CREATED = "Successfully created Web App.";
    public static final String UPDATE_WEBAPP = "Updating target Web App...";
    public static final String UPDATE_WEBAPP_DONE = "Successfully updated Web App.";
    public static final String STOP_APP = "Stopping Web App before deploying artifacts...";
    public static final String START_APP = "Starting Web App after deploying artifacts...";
    public static final String STOP_APP_DONE = "Successfully stopped Web App.";
    public static final String START_APP_DONE = "Successfully started Web App.";

    private Azure azure;
    private AzureWebAppExtension azureWebAppExtension;
    private AzureAuthHelper azureAuthHelper;
    protected DeploymentUtil util = new DeploymentUtil();

    public void setAzureWebAppExtension(AzureWebAppExtension azureWebAppExtension) {
        this.azureWebAppExtension = azureWebAppExtension;
        azureAuthHelper = new AzureAuthHelper(this);
    }

    public AzureWebAppExtension getAzureWebAppExtension() {
        return azureWebAppExtension;
    }

    @TaskAction
    void deploy() {
        try {
            getLogger().info(String.format(WEBAPP_DEPLOY_START, azureWebAppExtension.getAppName()));
            createOrUpdateWebApp();
            deployArtifacts();
            getLogger().info(String.format(WEBAPP_DEPLOY_SUCCESS, azureWebAppExtension.getAppName()));
        } catch (Exception ex) {
            throw new TaskExecutionException(this, ex);
        }
    }

    private void createOrUpdateWebApp() throws Exception {
        final WebApp app = getWebApp();
        if (app == null) {
            createWebApp();
        } else {
            updateWebApp(app);
        }
    }

    public WebApp getWebApp() throws AzureAuthFailureException {
        try {
            return getAzureClient().webApps().getByResourceGroup(azureWebAppExtension.getResourceGroup(), azureWebAppExtension.getAppName());
        } catch (AzureAuthFailureException authEx) {
            throw authEx;
        } catch (Exception ex) {
            // Swallow exception for non-existing web app
        }
        return null;
    }

    private void createWebApp() throws Exception {
        getLogger().info(WEBAPP_NOT_EXIST);
        getLogger().info(getFactory().getRuntimeHandler(this).getClass().getName());
        final WebApp.DefinitionStages.WithCreate withCreate = getFactory().getRuntimeHandler(this).defineAppWithRuntime();
        getFactory().getSettingsHandler(getProject()).processSettings(withCreate);
        withCreate.create();

        getLogger().info(WEBAPP_CREATED);
    }

    private void updateWebApp(final WebApp app) throws Exception {
        getLogger().info(UPDATE_WEBAPP);

        final WebApp.Update update = getFactory().getRuntimeHandler(this).updateAppRuntime(app);
        getFactory().getSettingsHandler(getProject()).processSettings(update);
        update.apply();

        getLogger().info(UPDATE_WEBAPP_DONE);
    }

    private void deployArtifacts() throws Exception {
        getLogger().info("Deploying artifacts");
        //getResources();
        /*if (resources == null || resources.isEmpty()) {
            getLog().info(NO_RESOURCES_CONFIG);
        } else */
        if (azureWebAppExtension.containerSettings != null) {
            getLogger().info("Nothing to upload to FTP");
        } else {
            try {
                util.beforeDeployArtifacts();
                getFactory().getArtifactHandler(getProject()).publish();
            } finally {
                util.afterDeployArtifacts();
            }
        }
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


    protected HandlerFactory getFactory() {
        return HandlerFactory.getInstance();
    }

    @Override
    public String getSubscriptionId() {
        return (String) getProject().getProperties().get("auth.subscriptionId");
    }

    // todo
    @Override
    public String getUserAgent() {
        return getName() + " " + getGroup();
//        return String.format("%s/%s %s:%s %s:%s", this.getName(), this.getGroup()
//                getPluginName(), getPluginVersion(),
//                INSTALLATION_ID_KEY, getInstallationId(),
//                SESSION_ID_KEY, getSessionId());
    }

    @Override
    public AuthenticationSetting getAuthenticationSetting() {
        if (getProject().hasProperty("auth.server.id") || getProject().hasProperty("auth.file")) {
            AuthenticationSetting authenticationSetting = new AuthenticationSetting();
            authenticationSetting.setServerId((String) getProject().getProperties().get("auth.server.id"));
            if (getProject().hasProperty("auth.file")) {
                authenticationSetting.setFile(new File((String) getProject().getProperties().get("auth.file")));
            }
            return authenticationSetting;
        }
        return null;
    }

    class DeploymentUtil {
        boolean isAppStopped = false;

        public void beforeDeployArtifacts() throws Exception {
            if (azureWebAppExtension.isStopAppDuringDeployment()) {
                getLogger().info(STOP_APP);

                getWebApp().stop();
                isAppStopped = true;

                getLogger().info(STOP_APP_DONE);
            }
        }

        public void afterDeployArtifacts() throws Exception {
            if (isAppStopped) {
                getLogger().info(START_APP);

                getWebApp().start();
                isAppStopped = false;

                getLogger().info(START_APP_DONE);
            }
        }
    }
}
