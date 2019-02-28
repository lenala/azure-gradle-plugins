package lenala.azure.gradle.webapp.helpers;

import lenala.azure.gradle.webapp.AzureWebAppExtension;
import lenala.azure.gradle.webapp.DeployTask;
import lenala.azure.gradle.webapp.configuration.AppService;
import lenala.azure.gradle.webapp.configuration.DockerImageType;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.AppServicePlan;
import com.microsoft.azure.management.appservice.OperatingSystem;
import com.microsoft.azure.management.appservice.RuntimeStack;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskExecutionException;

import static lenala.azure.gradle.webapp.helpers.CommonStringTemplates.APP_SERVICE_PROPERTY_MISSING_TEMPLATE;
import static lenala.azure.gradle.webapp.helpers.CommonStringTemplates.NOT_COMPATIBLE_WEBAPP_TEMPLATE;
import static lenala.azure.gradle.webapp.helpers.CommonStringTemplates.PROPERTY_MISSING_TEMPLATE;
import static lenala.azure.gradle.webapp.helpers.CommonStringTemplates.UNKNOWN_VALUE_TEMPLATE;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class WebAppUtils {
    private static final String SERVICE_PLAN_NOT_APPLICABLE = "The App Service Plan '%s' is not a %s Plan";

    private static final String CREATE_SERVICE_PLAN = "Creating App Service Plan '%s'...";
    private static final String SERVICE_PLAN_EXIST = "Found existing App Service Plan '%s' in Resource Group '%s'.";
    private static final String SERVICE_PLAN_CREATED = "Successfully created App Service Plan.";

    private static boolean isLinuxWebApp(final WebApp app) {
        return app.inner().kind().contains("linux");
    }

    public static void assureLinuxWebApp(final WebApp app) throws GradleException {
        if (!isLinuxWebApp(app)) {
            throw new GradleException(String.format(NOT_COMPATIBLE_WEBAPP_TEMPLATE, "Linux"));
        }
    }

    public static void assureWindowsWebApp(final WebApp app) throws TaskExecutionException {
        if (isLinuxWebApp(app)) {
            throw new GradleException(String.format(NOT_COMPATIBLE_WEBAPP_TEMPLATE, "Windows"));
        }
    }

    public static WebApp.DefinitionStages.WithDockerContainerImage defineLinuxApp(
            DeployTask task,
            final AppServicePlan plan
    ) throws Exception {
        assureLinuxPlan(plan);

        final String resourceGroup = task.getAzureWebAppExtension().getResourceGroup();
        final WebApp.DefinitionStages.ExistingLinuxPlanWithGroup existingLinuxPlanWithGroup = task.getAzureClient().webApps()
                .define(task.getAzureWebAppExtension().getAppName())
                .withExistingLinuxPlan(plan);
        return task.getAzureClient().resourceGroups().contain(resourceGroup) ?
                existingLinuxPlanWithGroup.withExistingResourceGroup(resourceGroup) :
                existingLinuxPlanWithGroup.withNewResourceGroup(resourceGroup);
    }

    private static void assureLinuxPlan(final AppServicePlan plan) throws GradleException {
        if (!plan.operatingSystem().equals(OperatingSystem.LINUX)) {
            throw new GradleException(String.format(SERVICE_PLAN_NOT_APPLICABLE,
                    plan.name(), OperatingSystem.LINUX.name()));
        }
    }

    public static WebApp.DefinitionStages.WithCreate defineWindowsApp(DeployTask task, final AppServicePlan plan)
            throws Exception {
        assureWindowsPlan(plan);

        if (task.getAzureWebAppExtension().getAppName() == null) {
            throw new GradleException(String.format(PROPERTY_MISSING_TEMPLATE, "azureWebapp.appName"));
        }

        final String resourceGroup = task.getAzureWebAppExtension().getResourceGroup();
        final WebApp.DefinitionStages.ExistingWindowsPlanWithGroup existingWindowsPlanWithGroup =  task.getAzureClient().webApps()
                .define(task.getAzureWebAppExtension().getAppName())
                .withExistingWindowsPlan(plan);
        return task.getAzureClient().resourceGroups().contain(resourceGroup) ?
                existingWindowsPlanWithGroup.withExistingResourceGroup(resourceGroup) :
                existingWindowsPlanWithGroup.withNewResourceGroup(resourceGroup);
    }

    private static void assureWindowsPlan(final AppServicePlan plan) throws GradleException {
        if (!plan.operatingSystem().equals(OperatingSystem.WINDOWS)) {
            throw new GradleException(String.format(SERVICE_PLAN_NOT_APPLICABLE,
                    plan.name(), OperatingSystem.WINDOWS.name()));
        }
    }

    public static AppServicePlan createOrGetAppServicePlan(DeployTask task, OperatingSystem os)
            throws Exception {
        AzureWebAppExtension extension = task.getAzureWebAppExtension();
        AppServicePlan plan = null;
        final String servicePlanResGrp = isNotEmpty(extension.getAppServicePlanResourceGroup())
                ? extension.getAppServicePlanResourceGroup() : extension.getResourceGroup();

        String servicePlanName = extension.getAppServicePlanName();
        if (isNotEmpty(servicePlanName)) {
            plan = task.getAzureClient().appServices().appServicePlans()
                    .getByResourceGroup(servicePlanResGrp, servicePlanName);
        } else {
            servicePlanName = SdkContext.randomResourceName("ServicePlan", 18);
        }

        final Azure azure = task.getAzureClient();
        if (plan == null) {
            task.getLogger().quiet(String.format(CREATE_SERVICE_PLAN, servicePlanName));

            final AppServicePlan.DefinitionStages.WithGroup withGroup = azure.appServices().appServicePlans()
                    .define(servicePlanName)
                    .withRegion(extension.getRegion());

            final AppServicePlan.DefinitionStages.WithPricingTier withPricingTier
                    = azure.resourceGroups().contain(servicePlanResGrp) ?
                    withGroup.withExistingResourceGroup(servicePlanResGrp) :
                    withGroup.withNewResourceGroup(servicePlanResGrp);

            plan = withPricingTier.withPricingTier(extension.getPricingTier())
                    .withOperatingSystem(os).create();

            task.getLogger().quiet(SERVICE_PLAN_CREATED);
        } else {
            task.getLogger().quiet(String.format(SERVICE_PLAN_EXIST, servicePlanName, servicePlanResGrp));
        }

        return plan;
    }

    public static DockerImageType getDockerImageTypeFromName(final AppService appService) {
        if (appService == null || StringUtils.isEmpty(appService.getImageName())) {
            return DockerImageType.NONE;
        }
        final boolean isCustomRegistry = isNotEmpty(appService.getRegistryUrl());
        final boolean isPrivate = isNotEmpty(appService.getServerId());

        Logging.getLogger(WebAppUtils.class)
                .quiet("ServerId: " + appService.getServerId() + " : " + System.getenv("SERVER_ID"));

        if (isCustomRegistry) {
            return isPrivate ? DockerImageType.PRIVATE_REGISTRY : DockerImageType.UNKNOWN;
        } else {
            return isPrivate ? DockerImageType.PRIVATE_DOCKER_HUB : DockerImageType.PUBLIC_DOCKER_HUB;
        }
    }

    public static RuntimeStack getLinuxRuntimeStackFromString(final String runtimeStack) {
        if (isNotEmpty(runtimeStack)) {
            if (runtimeStack.equalsIgnoreCase(RuntimeStack.TOMCAT_8_5_JRE8.toString())) {
                return RuntimeStack.TOMCAT_8_5_JRE8;
            } else if (runtimeStack.equalsIgnoreCase(RuntimeStack.TOMCAT_9_0_JRE8.toString())) {
                return RuntimeStack.TOMCAT_9_0_JRE8;
            } else if (runtimeStack.equalsIgnoreCase(RuntimeStack.WILDFLY_14_JRE8.toString())) {
                return RuntimeStack.WILDFLY_14_JRE8;
            } else if (runtimeStack.equalsIgnoreCase("jre8")) {
                return RuntimeStack.JAVA_8_JRE8;
            } else {
                throw new GradleException(String.format(UNKNOWN_VALUE_TEMPLATE, "appService.runtimeStack"));
            }
        }
        throw new GradleException(
                String.format(APP_SERVICE_PROPERTY_MISSING_TEMPLATE, "appService.runtimeStack", "LINUX"));
    }

    public static void assureDockerSettingsValid(final AppService appService) throws GradleException {
        if (appService == null) {
            throw new GradleException(String.format(PROPERTY_MISSING_TEMPLATE, "containerSettings"));
        }
        if (StringUtils.isEmpty(appService.getServerId())) {
            throw new GradleException(String.format(PROPERTY_MISSING_TEMPLATE, "containerSettings.serverId"));
        }
        if (StringUtils.isEmpty(appService.getUsername())) {
            throw new GradleException(String.format(PROPERTY_MISSING_TEMPLATE, "containerSettings.username"));
        }
        if (StringUtils.isEmpty(appService.getPassword())) {
            throw new GradleException(String.format(PROPERTY_MISSING_TEMPLATE, "containerSettings.password"));
        }
    }
}
