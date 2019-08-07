package lenala.azure.gradle.webapp;

import lenala.azure.gradle.webapp.auth.AuthConfiguration;
import lenala.azure.gradle.webapp.auth.AzureAuthFailureException;
import lenala.azure.gradle.webapp.auth.AzureAuthHelper;
import lenala.azure.gradle.webapp.configuration.Authentication;
import lenala.azure.gradle.webapp.configuration.DeployTarget;
import lenala.azure.gradle.webapp.configuration.DeploymentSlotDeployTarget;
import lenala.azure.gradle.webapp.configuration.WebAppDeployTarget;
import lenala.azure.gradle.webapp.handlers.HandlerFactory;
import lenala.azure.gradle.webapp.handlers.RuntimeHandler;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.DeploymentSlot;
import com.microsoft.azure.management.appservice.WebApp;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;

import static lenala.azure.gradle.webapp.helpers.CommonStringTemplates.PROPERTY_MISSING_TEMPLATE;

public class DeployTask extends DefaultTask implements AuthConfiguration {
    public static final String TASK_NAME = "azureWebappDeploy";

    private static final String AZURE_INIT_FAIL = "Failed to authenticate with Azure. Please check your configuration.";

    private static final String WEBAPP_DEPLOY_START = "Start deploying to Web App %s...";
    private static final String WEBAPP_DEPLOY_SUCCESS = "Successfully deployed Web App at https://%s.azurewebsites.net";
    private static final String WEBAPP_NOT_EXIST = "Target Web App doesn't exist. Creating a new one...";
    private static final String WEBAPP_CREATED = "Successfully created Web App.";
    private static final String UPDATE_WEBAPP = "Updating target Web App...";
    private static final String UPDATE_WEBAPP_DONE = "Successfully updated Web App.";
    private static final String STOP_APP = "Stopping Web App before deploying artifacts...";
    private static final String START_APP = "Starting Web App after deploying artifacts...";
    private static final String STOP_APP_DONE = "Successfully stopped Web App.";
    private static final String START_APP_DONE = "Successfully started Web App.";
    private static final String SLOT_SHOULD_EXIST_NOW = "Target deployment slot still does not exist." +
            "Please check if any error message during creation";

    private static final String SUBSCRIPTION_ID_KEY = "lenala.azure.subscriptionId";

    private Azure azure;
    private AzureWebAppExtension azureWebAppExtension;
    private WebApp app;
    private AzureAuthHelper azureAuthHelper;
    private DeploymentUtil util = new DeploymentUtil();

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
            getLogger().quiet(String.format(WEBAPP_DEPLOY_START, azureWebAppExtension.getAppName()));
            createOrUpdateWebApp();
            deployArtifacts();
            getLogger().quiet(String.format(WEBAPP_DEPLOY_SUCCESS, azureWebAppExtension.getAppName()));
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
        getLogger().quiet(WEBAPP_NOT_EXIST);
        RuntimeHandler runtimeHandler = getFactory().getRuntimeHandler(this);
        getLogger().quiet(runtimeHandler.getClass().getName());
        final WebApp.DefinitionStages.WithCreate withCreate = runtimeHandler.defineAppWithRuntime();
        getLogger().quiet("Processing settings");
        getFactory().getSettingsHandler(getProject()).processSettings(withCreate);
        getLogger().quiet("Creating WebApp");
        this.app = withCreate.create();

        getLogger().quiet(WEBAPP_CREATED);
    }

    private void updateWebApp(final WebApp app) throws Exception {
        getLogger().quiet(UPDATE_WEBAPP);

        final WebApp.Update update = getFactory().getRuntimeHandler(this).updateAppRuntime(app);
        getFactory().getSettingsHandler(getProject()).processSettings(update);
        update.apply();

        getLogger().quiet(UPDATE_WEBAPP_DONE);
        this.app = app;
    }

    private void deployArtifacts() throws Exception {
            try {
                getLogger().quiet("Deploying artifacts");
                util.beforeDeployArtifacts();

                DeployTarget target;

                if (azureWebAppExtension.getDeployment() == null) {
                    throw new GradleException(String.format(PROPERTY_MISSING_TEMPLATE, "deployment"));
                }

                if (azureWebAppExtension.getDeployment().getDeploymentSlot() != null) {
                    final String slotName = azureWebAppExtension.getDeployment().getDeploymentSlot();
                    final DeploymentSlot slot = getDeploymentSlot(app, slotName);
                    if (slot == null) {
                        throw new GradleException(SLOT_SHOULD_EXIST_NOW);
                    }
                    target = new DeploymentSlotDeployTarget(slot);
                } else {
                    target = new WebAppDeployTarget(getWebApp());
                }

                if (getFactory().getArtifactHandler(this) != null) {
                    getFactory().getArtifactHandler(this).publish(target);
                }
            } finally {
                util.afterDeployArtifacts();
            }
        }

    private DeploymentSlot getDeploymentSlot(final WebApp app, final String slotName) {
        DeploymentSlot slot = null;
        if (StringUtils.isNotEmpty(slotName)) {
            try {
                slot = app.deploymentSlots().getByName(slotName);
            } catch (NoSuchElementException deploymentSlotNotExistException) {
            }
        }
        return slot;
    }

    public Azure getAzureClient() throws AzureAuthFailureException {
        if (azure == null) {
            azure = azureAuthHelper.getAzureClient();
            if (azure == null) {
                throw new AzureAuthFailureException(AZURE_INIT_FAIL);
            }
        }
        return azure;
    }


    protected HandlerFactory getFactory() {
        return HandlerFactory.getInstance();
    }

    @Override
    public String getSubscriptionId() {
        return (String) getProject().getProperties().get(SUBSCRIPTION_ID_KEY);
    }

    // todo
    @Override
    public String getUserAgent() {
        return getName() + " " + getGroup();
    }

    @Override
    public Authentication getAuthenticationSettings() {
        Authentication authSetting = azureWebAppExtension.getAuthentication();
        if (authSetting == null) {
            throw new GradleException(String.format(PROPERTY_MISSING_TEMPLATE, "authentication"));
        }
        Map<String, ?> props = getProject().getProperties();
        for (Field f : authSetting.getClass().getDeclaredFields()) {
            try {
                String key = String.format("lenala.azure.auth.%s", f.getName());
                if (null == f.get(authSetting) && props.containsKey(key)) {
                    f.set(authSetting, props.get(key));
        }
            } catch (IllegalAccessException e) {
                // ignore
    }
        }
        return authSetting;
    }

    class DeploymentUtil {
        boolean isAppStopped = false;

        public void beforeDeployArtifacts() throws Exception {
            if (azureWebAppExtension.isStopAppDuringDeployment()) {
                getLogger().quiet(STOP_APP);

                getWebApp().stop();
                isAppStopped = true;

                getLogger().quiet(STOP_APP_DONE);
            }
        }

        public void afterDeployArtifacts() throws Exception {
            if (isAppStopped) {
                getLogger().quiet(START_APP);

                getWebApp().start();
                isAppStopped = false;

                getLogger().quiet(START_APP_DONE);
            }
        }
    }
}
