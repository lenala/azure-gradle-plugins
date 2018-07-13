/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.gradle.functions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.microsoft.azure.gradle.functions.configuration.FunctionConfiguration;
import com.microsoft.azure.gradle.functions.handlers.AnnotationHandler;
import com.microsoft.azure.gradle.functions.handlers.AnnotationHandlerImpl;
import org.apache.commons.io.FileUtils;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;


public class PackageTask extends FunctionsTask {
    private static final String SEARCH_FUNCTIONS = "Step 1 of 6: Searching for Azure Function entry points";
    private static final String FOUND_FUNCTIONS = " Azure Function entry point(s) found.";
    private static final String GENERATE_CONFIG = "Step 2 of 6: Generating Azure Function configurations";
    private static final String GENERATE_SKIP = "No Azure Functions found. Skip configuration generation.";
    private static final String GENERATE_DONE = "Generation done.";
    private static final String VALIDATE_CONFIG = "Step 3 of 6: Validating generated configurations";
    private static final String VALIDATE_SKIP = "No configurations found. Skip validation.";
    private static final String VALIDATE_DONE = "Validation done.";
    private static final String SAVE_HOST_JSON = "Step 4 of 6: Saving empty host.json";
    private static final String SAVE_FUNCTION_JSONS = "Step 5 of 6: Saving configurations to function.json";
    private static final String SAVE_SKIP = "No configurations found. Skip save.";
    private static final String SAVE_FUNCTION_JSON = "Starting processing function: ";
    private static final String SAVE_SUCCESS = "Successfully saved to ";
    private static final String COPY_JARS = "Step 6 of 6: Copying JARs to staging directory ";
    private static final String COPY_HOST_JSON = "Step 4 of 6: Copying existing host.json";
    private static final String COPY_SUCCESS = "Copied successfully.";
    private static final String BUILD_SUCCESS = "Successfully built Azure Functions.";

    private static final String FUNCTION_JSON = "function.json";
    private static final String HOST_JSON = "host.json";

    @TaskAction
    void packageFunction() {
        try {
            final AnnotationHandler handler = getAnnotationHandler();

            final Set<Method> methods = findAnnotatedMethods(handler);

            final Map<String, FunctionConfiguration> configMap = getFunctionConfigurations(handler, methods);

            validateFunctionConfigurations(configMap);

            final ObjectWriter objectWriter = getObjectWriter();

            writeHostJsonFile(objectWriter);

            copyLocalSettingsJson();

            writeFunctionJsonFiles(objectWriter, configMap);

            copyJarsToStageDirectory();

            getLogger().quiet(BUILD_SUCCESS);
        } catch (Exception ex) {
            throw new TaskExecutionException(this, ex);
        }
    }

    private AnnotationHandler getAnnotationHandler() throws Exception {
        return new AnnotationHandlerImpl(getLogger());
    }

    private Set<Method> findAnnotatedMethods(final AnnotationHandler handler) throws Exception {
        getLogger().quiet(SEARCH_FUNCTIONS);
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

    private URL getArtifactUrl() throws Exception {
        return null;
//        return this.getProject().Artifact().getFile().toURI().toURL();
        //        return this.getProject().getArtifact().getFile().toURI().toURL();
    }

    private URL getTargetClassUrl() throws Exception {
        return new File(getOutputDirectory()).toURI().toURL();
    }

    private Map<String, FunctionConfiguration> getFunctionConfigurations(final AnnotationHandler handler,
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

    private String getScriptFilePath() {
        return new StringBuilder()
                .append("..")
                .append(File.separator)
                .append(getFinalName())
                .append(".jar")
                .toString();
    }

    private void validateFunctionConfigurations(final Map<String, FunctionConfiguration> configMap) {
        getLogger().quiet(VALIDATE_CONFIG);
        if (configMap.size() == 0) {
            getLogger().quiet(VALIDATE_SKIP);
        } else {
            configMap.values().forEach(config -> config.validate());
            getLogger().quiet(VALIDATE_DONE);
        }
    }

    private void writeFunctionJsonFiles(final ObjectWriter objectWriter,
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

    private void writeFunctionJsonFile(final ObjectWriter objectWriter, final String functionName,
                                       final FunctionConfiguration config) throws IOException {
        getLogger().quiet(SAVE_FUNCTION_JSON + functionName);
        final File functionJsonFile = Paths.get(getDeploymentStageDirectory(), functionName, FUNCTION_JSON).toFile();
        writeObjectToFile(objectWriter, config, functionJsonFile);
        getLogger().quiet(SAVE_SUCCESS + functionJsonFile.getAbsolutePath());
    }

    private void writeHostJsonFile(final ObjectWriter objectWriter) throws IOException {
        final File srcHostJsonFile = new File(getProject().getProjectDir().getAbsolutePath() + "/host.json");
        final File destHostJsonFile = Paths.get(getDeploymentStageDirectory(), HOST_JSON).toFile();

        if (srcHostJsonFile.exists()) {
            getLogger().quiet(COPY_HOST_JSON);
            FileUtils.copyFile(srcHostJsonFile, destHostJsonFile);
            getLogger().quiet(COPY_SUCCESS);
        } else {
            getLogger().quiet(SAVE_HOST_JSON);
            writeObjectToFile(objectWriter, new Object(), destHostJsonFile);
            getLogger().quiet(SAVE_SUCCESS + destHostJsonFile.getAbsolutePath());
        }
    }

    private void writeObjectToFile(final ObjectWriter objectWriter, final Object object, final File targetFile)
            throws IOException {
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();
        objectWriter.writeValue(targetFile, object);
    }

    private ObjectWriter getObjectWriter() {
        return new ObjectMapper()
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .writerWithDefaultPrettyPrinter();
    }


    private void copyJarsToStageDirectory() throws IOException {
        final String stagingDirectory = getDeploymentStageDirectory();
        getLogger().quiet(COPY_JARS + stagingDirectory);
        String jarName = getProject().getTasks().getByPath("jar").getOutputs().getFiles().getAsPath();
        getLogger().quiet(jarName);
        FileUtils.copyFileToDirectory(new File(jarName), new File(getDeploymentStageDirectory()));

        getLogger().quiet(COPY_SUCCESS);
    }

    private void copyLocalSettingsJson() throws IOException {
        final String stagingDirectory = getDeploymentStageDirectory();
        getLogger().quiet("Copying local.settings.json...");
        FileUtils.copyFileToDirectory(new File(getProject().getProjectDir().getAbsolutePath() + "/local.settings.json"), new File(getDeploymentStageDirectory()));

        getLogger().quiet(COPY_SUCCESS);

    }
}
