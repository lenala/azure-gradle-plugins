package com.lenala.azure.gradle.webapp.handlers;

import com.lenala.azure.gradle.webapp.AzureWebAppExtension;
import com.lenala.azure.gradle.webapp.DeployTask;
import com.lenala.azure.gradle.webapp.configuration.AppService;
import com.lenala.azure.gradle.webapp.helpers.WebAppUtils;
import com.microsoft.azure.management.appservice.AppServicePlan;
import com.microsoft.azure.management.appservice.OperatingSystem;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.appservice.WebApp.DefinitionStages.WithCreate;
import org.gradle.api.GradleException;

public class PrivateDockerHubRuntimeHandlerImpl implements RuntimeHandler {
    private DeployTask task;
    private AzureWebAppExtension extension;

    public PrivateDockerHubRuntimeHandlerImpl(final DeployTask task) {
        this.task = task;
        this.extension = task.getAzureWebAppExtension();
    }

    @Override
    public WithCreate defineAppWithRuntime() throws Exception {
        final AppService appService = extension.getAppService();
        WebAppUtils.assureDockerSettingsValid(appService);

        final AppServicePlan plan = WebAppUtils.createOrGetAppServicePlan(task, OperatingSystem.LINUX);
        return WebAppUtils.defineLinuxApp(task, plan)
                .withPrivateDockerHubImage(appService.getImageName())
                .withCredentials(appService.getUsername(), appService.getPassword());
    }

    @Override
    public WebApp.Update updateAppRuntime(final WebApp app) throws GradleException {
        WebAppUtils.assureLinuxWebApp(app);
        final AppService appService = extension.getAppService();
        WebAppUtils.assureDockerSettingsValid(appService);
        return app.update()
                .withPrivateDockerHubImage(appService.getImageName())
                .withCredentials(appService.getUsername(), appService.getPassword());
    }
}
