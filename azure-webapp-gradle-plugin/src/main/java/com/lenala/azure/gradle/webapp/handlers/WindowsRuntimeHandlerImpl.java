package com.lenala.azure.gradle.webapp.handlers;

import com.lenala.azure.gradle.webapp.AzureWebAppExtension;
import com.lenala.azure.gradle.webapp.DeployTask;
import com.lenala.azure.gradle.webapp.helpers.WebAppUtils;
import com.microsoft.azure.management.appservice.AppServicePlan;
import com.microsoft.azure.management.appservice.OperatingSystem;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.appservice.WebApp.DefinitionStages.WithCreate;
import com.microsoft.azure.management.appservice.WebApp.Update;
import org.gradle.api.GradleException;

import static com.lenala.azure.gradle.webapp.helpers.CommonStringTemplates.PROPERTY_MISSING_TEMPLATE;

public class WindowsRuntimeHandlerImpl implements RuntimeHandler {
    private DeployTask task;
    private AzureWebAppExtension extension;

    public WindowsRuntimeHandlerImpl(final DeployTask task) {
        this.task = task;
        this.extension = task.getAzureWebAppExtension();
    }

    @Override
    public WithCreate defineAppWithRuntime() throws Exception {
        final AppServicePlan plan = WebAppUtils.createOrGetAppServicePlan(task, OperatingSystem.WINDOWS);
        final WithCreate withCreate = WebAppUtils.defineWindowsApp(task, plan);

        if (extension.getAppService().getJavaVersion() == null) {
            throw new GradleException(String.format(PROPERTY_MISSING_TEMPLATE, "appService.javaVersion"));
        }

        withCreate.withJavaVersion(extension.getAppService().getJavaVersion())
                .withWebContainer(extension.getAppService().getJavaWebContainer());
        return withCreate;
    }

    @Override
    public Update updateAppRuntime(final WebApp app) {
        WebAppUtils.assureWindowsWebApp(app);

        if (extension.getAppService().getJavaVersion() == null) {
            throw new GradleException(String.format(PROPERTY_MISSING_TEMPLATE, "appService.javaVersion"));
        }

        final Update update = app.update();
        update.withJavaVersion(extension.getAppService().getJavaVersion())
                .withWebContainer(extension.getAppService().getJavaWebContainer());
        return update;
    }
}
