package lenala.azure.gradle.webapp.handlers;

import lenala.azure.gradle.webapp.AzureWebAppExtension;
import lenala.azure.gradle.webapp.DeployTask;
import lenala.azure.gradle.webapp.helpers.WebAppUtils;
import com.microsoft.azure.management.appservice.AppServicePlan;
import com.microsoft.azure.management.appservice.OperatingSystem;
import com.microsoft.azure.management.appservice.WebApp;
import org.gradle.api.GradleException;

public class LinuxRuntimeHandlerImpl implements RuntimeHandler {
    private DeployTask task;
    private AzureWebAppExtension extension;

    public LinuxRuntimeHandlerImpl(final DeployTask task) {
        this.task = task;
        this.extension = task.getAzureWebAppExtension();
    }

    @Override
    public WebApp.DefinitionStages.WithCreate defineAppWithRuntime() throws Exception {
        final AppServicePlan plan = WebAppUtils.createOrGetAppServicePlan(task, OperatingSystem.LINUX);

        return WebAppUtils.defineLinuxApp(task, plan).withBuiltInImage(
                WebAppUtils.getLinuxRuntimeStackFromString(extension.getAppService().getRuntimeStack()));
    }

    @Override
    public WebApp.Update updateAppRuntime(WebApp app) throws GradleException {
        WebAppUtils.assureLinuxWebApp(app);
        return app.update().withBuiltInImage(
                WebAppUtils.getLinuxRuntimeStackFromString(extension.getAppService().getRuntimeStack()));
    }
}
