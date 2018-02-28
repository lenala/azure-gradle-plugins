/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.gradle.functions.auth;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.credentials.AzureCliCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.Azure.Authenticated;
import com.microsoft.rest.LogLevel;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * Helper class to authenticate with Azure
 */
public class AzureAuthHelper {
    public static final String CLIENT_ID = "client";
    private static final String TENANT_ID = "tenant";
    private static final String KEY = "key";
    private static final String CERTIFICATE = "certificate";
    private static final String CERTIFICATE_PASSWORD = "certificatePassword";
    private static final String ENVIRONMENT = "environment";

    private static final String AUTH_WITH_CLIENT_ID = "Authenticate with clientId: ";
    private static final String AUTH_WITH_FILE = "Authenticate with file: ";
    private static final String AUTH_WITH_AZURE_CLI = "Authenticate with Azure CLI 2.0";
    private static final String USE_KEY_TO_AUTH = "Use key to get Azure authentication token: ";
    private static final String USE_CERTIFICATE_TO_AUTH = "Use certificate to get Azure authentication token.";

    private static final String CLIENT_ID_NOT_CONFIG = "Client Id of your service principal is not configured.";
    private static final String TENANT_ID_NOT_CONFIG = "Tenant Id of your service principal is not configured.";
    private static final String KEY_NOT_CONFIG = "Key of your service principal is not configured.";
    private static final String CERTIFICATE_FILE_NOT_CONFIG = "Certificate of your service principal is not configured.";
    private static final String CERTIFICATE_FILE_READ_FAIL = "Failed to read certificate file: ";
    private static final String AUTH_FILE_NOT_CONFIG = "Authentication file is not configured.";
    private static final String AUTH_FILE_NOT_EXIST = "Authentication file does not exist: ";
    private static final String AUTH_FILE_READ_FAIL = "Failed to read authentication file: ";
    private static final String AZURE_CLI_AUTH_FAIL = "Failed to authenticate with Azure CLI 2.0";

    private AuthConfiguration config;
    private Logger logger = Logging.getLogger(AzureAuthHelper.class);

    /**
     * Constructor
     *
     * @param config
     */
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

    private LogLevel getLogLevel() {
        return logger.isDebugEnabled() ?
                LogLevel.BODY_AND_HEADERS :
                LogLevel.NONE;
    }

    private Azure.Configurable azureConfigure() {
        return Azure.configure()
                .withLogLevel(getLogLevel())
                .withUserAgent(config.getUserAgent());
    }

    private AzureEnvironment getAzureEnvironment(String environment) {
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

    private Authenticated getAuthObj() {
        Authenticated auth;
        // check if project has Azure authentication settings in build.gradle or gradle.properties
        boolean hasAuthSetting = config.hasAuthenticationSettings();
        if (hasAuthSetting) {
            auth = getAuthObjFromConfiguration(config);
            if (auth == null) {
                auth = getAuthObjFromFile(new File(config.getAuthFile()));
            }
        } else {
            auth = getAuthObjFromAzureCli();
        }
        return auth;
    }

    /**
     * Get Authenticated object by reading app token credentials from gradle.properties
     *
     * @return Authenticated object if configurations are correct; otherwise return null.
     */
    private Authenticated getAuthObjFromConfiguration(final AuthConfiguration config) {
        final ApplicationTokenCredentials credential = getAppTokenCredentials();
        if (credential == null) {
            return null;
        }

        final Authenticated auth = azureConfigure().authenticate(credential);
        if (auth != null) {
            logger.quiet(AUTH_WITH_CLIENT_ID + config.getAuthenticationSetting(CLIENT_ID));
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
            logger.debug(AUTH_FILE_NOT_CONFIG);
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
    private ApplicationTokenCredentials getAppTokenCredentials() {
        final String clientId = config.getAuthenticationSetting(CLIENT_ID);
        if (StringUtils.isEmpty(clientId)) {
            logger.quiet(CLIENT_ID_NOT_CONFIG);
            return null;
        }

        final String tenantId = config.getAuthenticationSetting(TENANT_ID);
        if (StringUtils.isEmpty(tenantId)) {
            logger.quiet(TENANT_ID_NOT_CONFIG);
            return null;
        }

        final String environment = config.getAuthenticationSetting(ENVIRONMENT);
        final AzureEnvironment azureEnvironment = getAzureEnvironment(environment);
        logger.quiet("Azure Management Endpoint: " + azureEnvironment.managementEndpoint());

        final String key = config.getAuthenticationSetting(KEY);
        if (!StringUtils.isEmpty(key)) {
            logger.quiet(USE_KEY_TO_AUTH);
            return new ApplicationTokenCredentials(clientId, tenantId, key, azureEnvironment);
        } else {
            logger.quiet(KEY_NOT_CONFIG);
        }

        final String certificate = config.getAuthenticationSetting(CERTIFICATE);
        if (StringUtils.isEmpty(certificate)) {
            logger.quiet(CERTIFICATE_FILE_NOT_CONFIG);
            return null;
        }

        final String certificatePassword = config.getAuthenticationSetting(CERTIFICATE_PASSWORD);
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
