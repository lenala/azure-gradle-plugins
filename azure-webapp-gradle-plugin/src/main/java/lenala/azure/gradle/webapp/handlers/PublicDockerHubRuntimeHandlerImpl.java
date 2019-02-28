package lenala.azure.gradle.webapp.handlers;

import lenala.azure.gradle.webapp.AzureWebAppExtension;
import lenala.azure.gradle.webapp.DeployTask;
import lenala.azure.gradle.webapp.configuration.AppService;
import lenala.azure.gradle.webapp.helpers.WebAppUtils;
import com.microsoft.azure.management.appservice.AppServicePlan;
import com.microsoft.azure.management.appservice.OperatingSystem;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.appservice.WebApp.DefinitionStages.WithCreate;
import com.microsoft.azure.management.appservice.WebApp.Update;
import org.gradle.api.GradleException;

public class PublicDockerHubRuntimeHandlerImpl implements RuntimeHandler {
    private DeployTask task;
    private AzureWebAppExtension extension;

    public PublicDockerHubRuntimeHandlerImpl(DeployTask task) {
        this.task = task;
        this.extension = task.getAzureWebAppExtension();
    }

    @Override
    public WithCreate defineAppWithRuntime() throws Exception {
        final AppServicePlan plan = WebAppUtils.createOrGetAppServicePlan(task, OperatingSystem.LINUX);

        return WebAppUtils.defineLinuxApp(task, plan)
                .withPublicDockerHubImage(extension.getAppService().getImageName());
    }

    @Override
    public Update updateAppRuntime(final WebApp app) throws GradleException {
        WebAppUtils.assureLinuxWebApp(app);
        final AppService appService = extension.getAppService();
        return app.update().withPublicDockerHubImage(appService.getImageName());
    }
}
