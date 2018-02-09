/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.gradle.functions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.microsoft.azure.gradle.functions.auth.AuthConfiguration;
import com.microsoft.azure.gradle.functions.auth.AzureAuthFailureException;
import com.microsoft.azure.gradle.functions.auth.AzureAuthHelper;
import com.microsoft.azure.gradle.functions.configuration.FunctionConfiguration;
import com.microsoft.azure.gradle.functions.handlers.AnnotationHandler;
import com.microsoft.azure.gradle.functions.handlers.AnnotationHandlerImpl;
import com.microsoft.azure.gradle.functions.helpers.Utils;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.FunctionApp;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

public class PackageTask  extends DefaultTask implements AuthConfiguration {
    public static final String SEARCH_FUNCTIONS = "Step 1 of 6: Searching for Azure Function entry points";
    public static final String FOUND_FUNCTIONS = " Azure Function entry point(s) found.";
    public static final String GENERATE_CONFIG = "Step 2 of 6: Generating Azure Function configurations";
    public static final String GENERATE_SKIP = "No Azure Functions found. Skip configuration generation.";
    public static final String GENERATE_DONE = "Generation done.";
    public static final String VALIDATE_CONFIG = "Step 3 of 6: Validating generated configurations";
    public static final String VALIDATE_SKIP = "No configurations found. Skip validation.";
    public static final String VALIDATE_DONE = "Validation done.";
    public static final String SAVE_HOST_JSON = "Step 4 of 6: Saving empty host.json";
    public static final String SAVE_FUNCTION_JSONS = "Step 5 of 6: Saving configurations to function.json";
    public static final String SAVE_SKIP = "No configurations found. Skip save.";
    public static final String SAVE_FUNCTION_JSON = "Starting processing function: ";
    public static final String SAVE_SUCCESS = "Successfully saved to ";
    public static final String COPY_JARS = "Step 6 of 6: Copying JARs to staging directory ";
    public static final String COPY_SUCCESS = "Copied successfully.";
    public static final String BUILD_SUCCESS = "Successfully built Azure Functions.";

    public static final String FUNCTION_JSON = "function.json";
    public static final String HOST_JSON = "host.json";

    private Azure azure;
    private AzureFunctionsExtension azureFunctionsExtension;
    private AzureAuthHelper azureAuthHelper;

    @TaskAction
    void packageFunction() {
        try {
            final AnnotationHandler handler = getAnnotationHandler();

            final Set<Method> methods = findAnnotatedMethods(handler);

            final Map<String, FunctionConfiguration> configMap = getFunctionConfigurations(handler, methods);

            validateFunctionConfigurations(configMap);

            final ObjectWriter objectWriter = getObjectWriter();

            writeEmptyHostJsonFile(objectWriter);

            writeFunctionJsonFiles(objectWriter, configMap);

            copyJarsToStageDirectory();

            getLogger().quiet(BUILD_SUCCESS);
        } catch (Exception ex) {
            throw new TaskExecutionException(this, ex);
        }
    }

    protected AnnotationHandler getAnnotationHandler() throws Exception {
        return new AnnotationHandlerImpl(/*getLog()*/);
    }

    protected Set<Method> findAnnotatedMethods(final AnnotationHandler handler) throws Exception {
        getLogger().quiet("SEARCH_FUNCTIONS");
        Set<Method> functions;
        try {
            getLogger().quiet("ClassPath to resolve: " + getTargetClassUrl());
            functions = handler.findFunctions(getTargetClassUrl());
        } catch (NoClassDefFoundError e) {
            // fallback to reflect through artifact url
            getLogger().quiet("ClassPath to resolve: " + getArtifactUrl());
            functions = handler.findFunctions(getArtifactUrl());
        }
        getLogger().quiet(functions.size() + FOUND_FUNCTIONS);
        return functions;
    }

    protected Map<String, FunctionConfiguration> getFunctionConfigurations(final AnnotationHandler handler,
                                                                           final Set<Method> methods) throws Exception {
        getLogger().quiet(GENERATE_CONFIG);
        final Map<String, FunctionConfiguration> configMap = handler.generateConfigurations(methods);
        if (configMap.size() == 0) {
            getLogger().quiet(GENERATE_SKIP);
        } else {
            final String scriptFilePath = getScriptFilePath();
            configMap.values().forEach(config -> config.setScriptFile(scriptFilePath));
            getLogger().quiet(GENERATE_DONE);
        }

        return configMap;
    }

    protected String getScriptFilePath() {
        return new StringBuilder()
                .append("..")
                .append(File.separator)
                .append(getFinalName())
                .append(".jar")
                .toString();
    }

    protected void validateFunctionConfigurations(final Map<String, FunctionConfiguration> configMap) {
        getLogger().quiet(VALIDATE_CONFIG);
        if (configMap.size() == 0) {
            getLogger().quiet(VALIDATE_SKIP);
        } else {
            configMap.values().forEach(config -> config.validate());
            getLogger().quiet(VALIDATE_DONE);
        }
    }

    protected void writeFunctionJsonFiles(final ObjectWriter objectWriter,
                                          final Map<String, FunctionConfiguration> configMap) throws IOException {
        getLogger().quiet(SAVE_FUNCTION_JSONS);
        if (configMap.size() == 0) {
            getLogger().quiet(SAVE_SKIP);
        } else {
            for (final Map.Entry<String, FunctionConfiguration> config : configMap.entrySet()) {
                writeFunctionJsonFile(objectWriter, config.getKey(), config.getValue());
            }
        }
    }

    protected void writeFunctionJsonFile(final ObjectWriter objectWriter, final String functionName,
                                         final FunctionConfiguration config) throws IOException {
        getLogger().quiet(SAVE_FUNCTION_JSON + functionName);
        final File functionJsonFile = Paths.get(getDeploymentStageDirectory(), functionName, FUNCTION_JSON).toFile();
        writeObjectToFile(objectWriter, config, functionJsonFile);
        getLogger().quiet(SAVE_SUCCESS + functionJsonFile.getAbsolutePath());
    }

    protected void writeEmptyHostJsonFile(final ObjectWriter objectWriter) throws IOException {
        getLogger().quiet(SAVE_HOST_JSON);
        final File hostJsonFile = Paths.get(getDeploymentStageDirectory(), HOST_JSON).toFile();
        writeObjectToFile(objectWriter, new Object(), hostJsonFile);
        getLogger().quiet(SAVE_SUCCESS + hostJsonFile.getAbsolutePath());
    }

    protected void writeObjectToFile(final ObjectWriter objectWriter, final Object object, final File targetFile)
            throws IOException {
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();
        objectWriter.writeValue(targetFile, object);
    }

    protected ObjectWriter getObjectWriter() {
        return new ObjectMapper()
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .writerWithDefaultPrettyPrinter();
    }


    protected void copyJarsToStageDirectory() throws IOException {
        final String stagingDirectory = getDeploymentStageDirectory();
        getLogger().quiet(COPY_JARS + stagingDirectory);
        Utils.copyResources(
                getProject(),
                getSession(),
                getMavenResourcesFiltering(),
                getResources(),
                stagingDirectory);
        getLogger().quiet(COPY_SUCCESS);
    }


    public void setAzureFunctionsExtension(AzureFunctionsExtension azureFunctionsExtension) {
        this.azureFunctionsExtension = azureFunctionsExtension;
        azureAuthHelper = new AzureAuthHelper(this);
    }



    @Override
    public String getUserAgent() {
        return getName() + " " + getGroup();
//        return String.format("%s/%s %s:%s %s:%s", this.getName(), this.getGroup()
//                getPluginName(), getPluginVersion(),
//                INSTALLATION_ID_KEY, getInstallationId(),
//                SESSION_ID_KEY, getSessionId());
    }

    @Override
    public String getSubscriptionId() {
        return (String) getProject().getProperties().get("subscriptionId");
    }

    @Override
    public boolean hasAuthenticationSettings() {
        return getProject().getProperties().containsKey(AzureAuthHelper.CLIENT_ID) || azureFunctionsExtension.getAuthFile() != null;
    }

    @Override
    public String getAuthenticationSetting(String key) {
        return (String) getProject().getProperties().get(key);
    }

    @Override
    public String getAuthFile() {
        return azureFunctionsExtension.getAuthFile();
    }


    public String getDeploymentStageDirectory() {
        return Paths.get(getBuildDirectoryAbsolutePath(),
                AZURE_FUNCTIONS,
                getAppName()).toString();
    }

    public FunctionApp getFunctionApp() throws AzureAuthFailureException {
        try {
            return getAzureClient().appServices().functionApps().getByResourceGroup(getResourceGroup(), getAppName());
        } catch (AzureAuthFailureException authEx) {
            throw authEx;
        } catch (Exception ex) {
            // Swallow exception for non-existing function app
        }
        return null;
    }
}
