/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.gradle.functions;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import org.gradle.process.internal.DefaultExecActionFactory;
import org.gradle.process.internal.ExecAction;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;

public class RunTask  extends FunctionsTask {
    private static final String STAGE_DIR_FOUND = "Azure Functions stage directory found at: ";
    private static final String STAGE_DIR_NOT_FOUND =
            "Stage directory not found. Please run package task first.";
    private static final String RUNTIME_FOUND = "Azure Functions Core Tools found.";
    private static final String RUNTIME_NOT_FOUND = "Azure Functions Core Tools not found. " +
            "Please run 'npm i -g azure-functions-core-tools@core' to install Azure Functions Core Tools first.";
    private static final String RUN_FUNCTIONS_FAILURE = "Failed to run Azure Functions. Please checkout console output.";
    private static final String START_RUN_FUNCTIONS = "Starting running Azure Functions...";

    private static final String WINDOWS_FUNCTION_RUN = "cd /D %s && func function run %s --no-interactive";
    private static final String LINUX_FUNCTION_RUN = "cd %s; func function run %s --no-interactive";
    private static final String WINDOWS_HOST_START = "cd /D %s && func host start";
    private static final String LINUX_HOST_START = "cd %s; func host start";

    private String targetFunction;
    private String functionInputString;
    private String functionInputFile;

    public String getTargetFunction() {
        return targetFunction;
    }

    public void setTargetFunction(String targetFunction) {
        this.targetFunction = targetFunction;
    }

    public void setFunctionInputString(String functionInputString) {
        this.functionInputString = functionInputString;
    }

    public String getInputString() {
        return functionInputString;
    }

    public void setFunctionInputFile(String functionInputFile) {
        this.functionInputFile = functionInputFile;
    }

    public String getInputFile() {
        return functionInputFile;
    }

    @TaskAction
    void runFunction() {
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

    private String[] getCheckRuntimeCommand() {
        return buildCommand("func");
    }

    private String[] getRunFunctionCommand() {
        return StringUtils.isEmpty(getTargetFunction()) ?
                getStartFunctionHostCommand() :
                getRunSingleFunctionCommand();
    }

    private String[] getRunSingleFunctionCommand() {
        String command = format(getRunFunctionTemplate(), getDeploymentStageDirectory(), getTargetFunction());
        if (StringUtils.isNotEmpty(getInputString())) {
            command = command.concat(" -c ").concat(getInputString());
        } else if (getInputFile() != null) {
            command = command.concat(" -f ").concat(new File(getInputFile()).getAbsolutePath());
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

        ExecAction action = new DefaultExecActionFactory(getServices().get(FileResolver.class)).newExecAction();
        action.setCommandLine(command);
        action.execute();
//        final ProcessBuilder.Redirect redirect = getStdoutRedirect(showStdout);
//        final Process process = new ProcessBuilder(command)
//                .redirectOutput(redirect)
//                .redirectErrorStream(true)
//                .start();
//
//        process.waitFor();
//
//        handleExitValue(process.exitValue(), validReturnCodes, errorMessage, process.getInputStream());
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
