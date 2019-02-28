package lenala.azure.gradle.webapp.auth;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.credentials.AzureCliCredentials;
import lenala.azure.gradle.webapp.configuration.Authentication;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.Azure.Authenticated;
import com.microsoft.rest.LogLevel;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

import static lenala.azure.gradle.webapp.helpers.CommonStringTemplates.PROPERTY_MISSING_TEMPLATE;

/**
 * Helper class to authenticate with Azure.
 */
public class AzureAuthHelper {
    private static final String AUTH_WITH_CLIENT_ID = "Authenticate with clientId: ";
    private static final String AUTH_WITH_FILE = "Authenticate with file: ";
    private static final String AUTH_WITH_AZURE_CLI = "Authenticate with Azure CLI 2.0";
    private static final String USE_KEY_TO_AUTH = "Use key to get Azure authentication token: ";
    private static final String USE_CERTIFICATE_TO_AUTH = "Use certificate to get Azure authentication token.";
    private static final String CERTIFICATE_FILE_READ_FAIL = "Failed to read certificate file: ";
    private static final String AUTH_FILE_NOT_EXIST = "Authentication file does not exist: ";
    private static final String AUTH_FILE_READ_FAIL = "Failed to read authentication file: ";
    private static final String AZURE_CLI_AUTH_FAIL = "Failed to authenticate with Azure CLI 2.0";

    protected AuthConfiguration config;
    private Logger logger = Logging.getLogger(AzureAuthHelper.class);

    public AzureAuthHelper(final AuthConfiguration config) {
        if (config == null) {
            throw new NullPointerException();
        }
        this.config = config;
    }

    public Azure getAzureClient() {
        final Authenticated auth = getAuthObj();
        if (auth == null) {
            return null;
        }
        try {
            final String subscriptionId = config.getSubscriptionId();
            return StringUtils.isEmpty(subscriptionId) ?
                    auth.withDefaultSubscription() :
                    auth.withSubscription(subscriptionId);
        } catch (Exception e) {
            logger.debug("", e);
        }
        return null;
    }

    protected LogLevel getLogLevel() {
        return logger.isDebugEnabled() ?
                LogLevel.BODY_AND_HEADERS :
                LogLevel.NONE;
    }

    protected Azure.Configurable azureConfigure() {
        return Azure.configure()
                .withLogLevel(getLogLevel())
                .withUserAgent(config.getUserAgent());
    }

    protected AzureEnvironment getAzureEnvironment(String environment) {
        if (StringUtils.isEmpty(environment)) {
            return AzureEnvironment.AZURE;
        }

        switch (environment.toUpperCase(Locale.ENGLISH)) {
            case "AZURE_CHINA":
                return AzureEnvironment.AZURE_CHINA;
            case "AZURE_GERMANY":
                return AzureEnvironment.AZURE_GERMANY;
            case "AZURE_US_GOVERNMENT":
                return AzureEnvironment.AZURE_US_GOVERNMENT;
            default:
                return AzureEnvironment.AZURE;
        }
    }

    protected Authenticated getAuthObj() {
        Authenticated auth;
        // check if project has Azure authentication settings in build.gradle
        // or gradle.properties or in environment variables
        final Authentication authSetting = config.getAuthenticationSettings();
        if (authSetting.getType() == null) {
            throw new GradleException(String.format(PROPERTY_MISSING_TEMPLATE, "authentication.type"));
        }
        switch (authSetting.getType()) {
            case FILE:
                if (authSetting.getFile() != null) {
                    return getAuthObjFromFile(new File(authSetting.getFile()));
        } else {
                    logger.quiet("Failed to get authentication file, please make sure it is specified.");
                    return null;
        }
            case PROPERTIES:
                return getAuthObjFromConfiguration(authSetting);
            case AZURECLI:
                return getAuthObjFromAzureCli();
            default:
                logger.error("Unrecognized authentication type.");
                return null;
        }
    }

    /**
     * Get Authenticated object by reading app token credentials from gradle.properties
     *
     * @return Authenticated object if configurations are correct; otherwise return null.
     */
    private Authenticated getAuthObjFromConfiguration(final Authentication authSetting) {
        final ApplicationTokenCredentials credential = getAppTokenCredentials(authSetting);
        if (credential == null) {
            logger.quiet("Authentication info for Azure is not valid.");
            return null;
        }

        final Authenticated auth = azureConfigure().authenticate(credential);
        if (auth != null) {
            logger.quiet(AUTH_WITH_CLIENT_ID + authSetting.getClient());
        }
        return auth;
    }

    /**
     * Get Authenticated object using file.
     *
     * @param authFile Authentication file object.
     * @return Authenticated object of file is valid; otherwise return null.
     */
    private Authenticated getAuthObjFromFile(final File authFile) {
        if (authFile == null) {
            logger.debug(String.format(PROPERTY_MISSING_TEMPLATE, "authentication.file"));
            return null;
        }

        if (!authFile.exists()) {
            logger.error(AUTH_FILE_NOT_EXIST + authFile.getAbsolutePath());
            return null;
        }

        try {
            final Authenticated auth = azureConfigure().authenticate(authFile);
            if (auth != null) {
                logger.quiet(AUTH_WITH_FILE + authFile.getAbsolutePath());
            }
            return auth;
        } catch (Exception e) {
            logger.error(AUTH_FILE_READ_FAIL + authFile.getAbsolutePath(), e);
        }
        return null;
    }

    /**
     * Get Authenticated object using authentication file from Azure CLI 2.0
     *
     * @return Authenticated object if Azure CLI 2.0 is logged in correctly; otherwise return null.
     */
    private Authenticated getAuthObjFromAzureCli() {
        try {
            final Authenticated auth = azureConfigure().authenticate(AzureCliCredentials.create());
            if (auth != null) {
                logger.quiet(AUTH_WITH_AZURE_CLI);
            }
            return auth;
        } catch (Exception e) {
            logger.debug(AZURE_CLI_AUTH_FAIL, e);
        }
        return null;
    }

    /**
     * Get ApplicationTokenCredentials from authentication settings in gradle.properties.
     *
     * @return ApplicationTokenCredentials object if configuration is correct; otherwise return null.
     */
    private ApplicationTokenCredentials getAppTokenCredentials(Authentication authSetting) {
        final String clientId = authSetting.getClient();
        if (StringUtils.isEmpty(clientId)) {
            logger.quiet(String.format(PROPERTY_MISSING_TEMPLATE, "authentication.client"));
            return null;
        }

        final String tenantId = authSetting.getTenant();
        if (StringUtils.isEmpty(tenantId)) {
            logger.quiet(String.format(PROPERTY_MISSING_TEMPLATE, "authentication.tenant"));
            return null;
        }

        final String environment = authSetting.getEnvironment();
        final AzureEnvironment azureEnvironment = getAzureEnvironment(environment);
        logger.quiet("Azure Management Endpoint: " + azureEnvironment.managementEndpoint());

        final String key = authSetting.getKey();
        if (!StringUtils.isEmpty(key)) {
            logger.quiet(USE_KEY_TO_AUTH);
            return new ApplicationTokenCredentials(clientId, tenantId, key, azureEnvironment);
        } else {
            logger.quiet(String.format(PROPERTY_MISSING_TEMPLATE, "authentication.key"));
        }

        final String certificate = authSetting.getCertificate();
        if (StringUtils.isEmpty(certificate)) {
            logger.quiet(String.format(PROPERTY_MISSING_TEMPLATE, "authentication.certificate"));
            return null;
        }

        final String certificatePassword = authSetting.getCertificatePassword();
        try {
            byte[] cert;
            cert = Files.readAllBytes(Paths.get(certificate, new String[0]));
            logger.quiet(USE_CERTIFICATE_TO_AUTH + certificate);
            return new ApplicationTokenCredentials(clientId, tenantId, cert, certificatePassword, azureEnvironment);
        } catch (Exception e) {
            logger.quiet(CERTIFICATE_FILE_READ_FAIL + certificate);
        }
        return null;
    }
}
