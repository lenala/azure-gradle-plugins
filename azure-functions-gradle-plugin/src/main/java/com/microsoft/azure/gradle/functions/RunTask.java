/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.gradle.functions;

import com.microsoft.azure.gradle.functions.auth.AzureAuthHelper;
import com.microsoft.azure.management.Azure;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;

public class RunTask  extends FunctionsTask {
    public static final String STAGE_DIR_FOUND = "Azure Functions stage directory found at: ";
    public static final String STAGE_DIR_NOT_FOUND =
            "Stage directory not found. Please run mvn:package or azure-functions:package first.";
    public static final String RUNTIME_FOUND = "Azure Functions Core Tools found.";
    public static final String RUNTIME_NOT_FOUND = "Azure Functions Core Tools not found. " +
            "Please run 'npm i -g azure-functions-core-tools@core' to install Azure Functions Core Tools first.";
    public static final String RUN_FUNCTIONS_FAILURE = "Failed to run Azure Functions. Please checkout console output.";
    public static final String START_RUN_FUNCTIONS = "Starting running Azure Functions...";

    public static final String WINDOWS_FUNCTION_RUN = "cd /D %s && func function run %s --no-interactive";
    public static final String LINUX_FUNCTION_RUN = "cd %s; func function run %s --no-interactive";
    public static final String WINDOWS_HOST_START = "cd /D %s && func host start";
    public static final String LINUX_HOST_START = "cd %s; func host start";
    private Azure azure;
    private AzureFunctionsExtension azureFunctionsExtension;
    private AzureAuthHelper azureAuthHelper;

    public void setAzureFunctionsExtension(AzureFunctionsExtension azureFunctionsExtension) {
        this.azureFunctionsExtension = azureFunctionsExtension;
        azureAuthHelper = new AzureAuthHelper(this);
    }

    public String getTargetFunction() {
        return "";//targetFunction;
    }

    public String getInputString() {
        return "";//functionInputString;
    }

    public File getInputFile() {
        return null;//functionInputFile;
    }

    @TaskAction
    void packageFunction() {
        try {
            checkStageDirectoryExistence();

            checkRuntimeExistence();

            runFunctions();
        } catch (Exception ex) {
            throw new TaskExecutionException(this, ex);
        }
    }

    private void checkStageDirectoryExistence() throws Exception {
        runCommand(getCheckStageDirectoryCommand(), false, getDefaultValidReturnCodes(), STAGE_DIR_NOT_FOUND);
        getLogger().quiet(STAGE_DIR_FOUND + getDeploymentStageDirectory());
    }

    private void checkRuntimeExistence() throws Exception {
        runCommand(getCheckRuntimeCommand(), false, getDefaultValidReturnCodes(), RUNTIME_NOT_FOUND);
        getLogger().quiet(RUNTIME_FOUND);
    }

    private void runFunctions() throws Exception {
        getLogger().quiet(START_RUN_FUNCTIONS);
        runCommand(getRunFunctionCommand(), true, getValidReturnCodes(), RUN_FUNCTIONS_FAILURE);
    }

    private String[] getCheckStageDirectoryCommand() {
        final String command = format(isWindows() ? "cd /D %s" : "cd %s", getDeploymentStageDirectory());
        return buildCommand(command);
    }

    protected String[] getCheckRuntimeCommand() {
        return buildCommand("func");
    }

    protected String[] getRunFunctionCommand() {
        return StringUtils.isEmpty(getTargetFunction()) ?
                getStartFunctionHostCommand() :
                getRunSingleFunctionCommand();
    }

    protected String[] getRunSingleFunctionCommand() {
        String command = format(getRunFunctionTemplate(), getDeploymentStageDirectory(), getTargetFunction());
        if (StringUtils.isNotEmpty(getInputString())) {
            command = command.concat(" -c ").concat(getInputString());
        } else if (getInputFile() != null) {
            command = command.concat(" -f ").concat(getInputFile().getAbsolutePath());
        }
        return buildCommand(command);
    }

    private String getRunFunctionTemplate() {
        return isWindows() ? WINDOWS_FUNCTION_RUN : LINUX_FUNCTION_RUN;
    }

    private String[] getStartFunctionHostCommand() {
        final String command = format(getStartFunctionHostTemplate(), getDeploymentStageDirectory());
        return buildCommand(command);
    }

    private String getStartFunctionHostTemplate() {
        return isWindows() ? WINDOWS_HOST_START : LINUX_HOST_START;
    }

    private String[] buildCommand(final String command) {
        return isWindows() ?
                new String[]{"cmd.exe", "/c", command} :
                new String[]{"sh", "-c", command};
    }

    private boolean isWindows() {
        return SystemUtils.IS_OS_WINDOWS;
    }

    private List<Long> getDefaultValidReturnCodes() {
        return Arrays.asList(0L);
    }

    private List<Long> getValidReturnCodes() {
        return isWindows() ?
                // Windows return code of CTRL-C is 3221225786
                Arrays.asList(0L, 3221225786L) :
                // Linux return code of CTRL-C is 130
                Arrays.asList(0L, 130L);
    }

    private void runCommand(final String[] command, final boolean showStdout, final List<Long> validReturnCodes,
                              final String errorMessage) throws Exception {
        getLogger().quiet("Executing command: " + StringUtils.join(command, " "));

        final ProcessBuilder.Redirect redirect = getStdoutRedirect(showStdout);
        final Process process = new ProcessBuilder(command)
                .redirectOutput(redirect)
                .redirectErrorStream(true)
                .start();

        process.waitFor();

        handleExitValue(process.exitValue(), validReturnCodes, errorMessage, process.getInputStream());
    }

    private ProcessBuilder.Redirect getStdoutRedirect(boolean showStdout) {
        return showStdout ? ProcessBuilder.Redirect.INHERIT : ProcessBuilder.Redirect.PIPE;
    }

    private void handleExitValue(int exitValue, final List<Long> validReturnCodes, final String errorMessage,
                                 final InputStream inputStream) throws Exception {
        getLogger().quiet("Process exit value: " + exitValue);
        if (!validReturnCodes.contains(Integer.toUnsignedLong(exitValue))) {
            // input stream is a merge of standard output and standard error of the sub-process
            showErrorIfAny(inputStream);
            getLogger().quiet(errorMessage);
            throw new Exception(errorMessage);
        }
    }

    private void showErrorIfAny(final InputStream inputStream) throws Exception {
        if (inputStream != null) {
            final String input = IOUtils.toString(inputStream);
            getLogger().quiet(StringUtils.strip(input, "\n")); // error?
        }
    }
}
