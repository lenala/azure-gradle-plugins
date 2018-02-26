/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.gradle.functions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.gradle.functions.template.FunctionTemplate;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.lang.System.out;
import static javax.lang.model.SourceVersion.isName;
import static org.apache.commons.io.CopyUtils.copy;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class AddTask extends FunctionsTask {
    private static final String LOAD_TEMPLATES = "Step 1 of 4: Load all function templates";
    private static final String LOAD_TEMPLATES_DONE = "Successfully loaded all function templates";
    private static final String LOAD_TEMPLATES_FAIL = "Failed to load all function templates.";
    private static final String FIND_TEMPLATE = "Step 2 of 4: Select function template";
    private static final String FIND_TEMPLATE_DONE = "Successfully found function template: ";
    private static final String FIND_TEMPLATE_FAIL = "Function template not found: ";
    private static final String PREPARE_PARAMS = "Step 3 of 4: Prepare required parameters";
    private static final String FOUND_VALID_VALUE = "Found valid value. Skip user input.";
    private static final String SAVE_FILE = "Step 4 of 4: Saving function to file";
    private static final String SAVE_FILE_DONE = "Successfully saved new function at ";
    private static final String FILE_EXIST = "Function already exists at %s. Please specify a different function name.";
    private static final String FUNCTION_NAME_REGEXP = "^[a-zA-Z][a-zA-Z\\d_\\-]*$";

    private File basedir;

    private List<String> compileSourceRoots;

    /**
     * Package name of the new function.
     */
    private String functionPackageName;

    /**
     * Name of the new function.
     */
    private String functionName;

    /**
     * Template for the new function
     */
    private String functionTemplate;

    public String getFunctionPackageName() {
        return functionPackageName;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getClassName() {
        return getFunctionName().replace('-', '_');
    }

    public String getFunctionTemplate() {
        return functionTemplate;
    }

    protected String getBasedir() {
        if (basedir == null) {
            basedir = getProject().getProjectDir();
        }
        return basedir.getAbsolutePath();
    }

    public void setBasedir(File basedir) {
        this.basedir = basedir;
    }

    protected String getSourceRoot() {
        return compileSourceRoots == null || compileSourceRoots.isEmpty() ?
                Paths.get(getBasedir(), "src", "main", "java").toString() :
                compileSourceRoots.get(0);
    }

    protected void setFunctionPackageName(String functionPackageName) {
        this.functionPackageName = StringUtils.lowerCase(functionPackageName);
    }

    public void setFunctionName(String functionName) {
        this.functionName = StringUtils.capitalize(functionName);
    }

    public void setFunctionTemplate(String functionTemplate) {
        this.functionTemplate = functionTemplate;
    }


    @TaskAction
    void add() {
        try {
            final List<FunctionTemplate> templates = loadAllFunctionTemplates();

            final FunctionTemplate template = getFunctionTemplate(templates);

            final Map params = prepareRequiredParameters(template);

            final String newFunctionClass = substituteParametersInTemplate(template, params);

            saveNewFunctionToFile(newFunctionClass);
        } catch (Exception ex) {
            throw new TaskExecutionException(this, ex);
        }
    }

    private List<FunctionTemplate> loadAllFunctionTemplates() throws Exception {
        getLogger().quiet(LOAD_TEMPLATES);

        try (final InputStream is = AddTask.class.getResourceAsStream("/templates.json")) {
            final String templatesJsonStr = IOUtils.toString(is);
            final List<FunctionTemplate> templates = parseTemplateJson(templatesJsonStr);
            getLogger().quiet(LOAD_TEMPLATES_DONE);
            return templates;
        } catch (Exception e) {
            getLogger().quiet(LOAD_TEMPLATES_FAIL);
            throw e;
        }
    }

    private List<FunctionTemplate> parseTemplateJson(final String templateJson) throws Exception {
        final FunctionTemplate[] templates = new ObjectMapper().readValue(templateJson, FunctionTemplate[].class);
        return Arrays.asList(templates);
    }

    private FunctionTemplate getFunctionTemplate(final List<FunctionTemplate> templates) throws Exception {
        getLogger().quiet(FIND_TEMPLATE);

        if (settings != null/* && !settings.isInteractiveMode()*/) {
            assureInputInBatchMode(getFunctionTemplate(),
                    str -> getTemplateNames(templates)
                            .stream()
                            .filter(Objects::nonNull)
                            .anyMatch(o -> o.equalsIgnoreCase(str)),
                    this::setFunctionTemplate,
                    true);
        } else {
            assureInputFromUser("template for new function",
                    getFunctionTemplate(),
                    getTemplateNames(templates),
                    this::setFunctionTemplate);
        }

        return findTemplateByName(templates, getFunctionTemplate());
    }

    private List<String> getTemplateNames(final List<FunctionTemplate> templates) {
        return templates.stream().map(t -> t.getMetadata().getName()).collect(Collectors.toList());
    }

    private FunctionTemplate findTemplateByName(final List<FunctionTemplate> templates, final String templateName)
            throws Exception {
        getLogger().quiet("Selected function template: " + templateName);
        final Optional<FunctionTemplate> template = templates.stream()
                .filter(t -> t.getMetadata().getName().equalsIgnoreCase(templateName))
                .findFirst();

        if (template.isPresent()) {
            getLogger().quiet(FIND_TEMPLATE_DONE + templateName);
            return template.get();
        }

        throw new Exception(FIND_TEMPLATE_FAIL + templateName);
    }

    private Map<String, String> prepareRequiredParameters(final FunctionTemplate template) {
        getLogger().quiet(PREPARE_PARAMS);

        prepareFunctionName();

        preparePackageName();

        final Map<String, String> params = new HashMap<>();
        params.put("functionName", getFunctionName());
        params.put("className", getClassName());
        params.put("packageName", getFunctionPackageName());

        prepareTemplateParameters(template, params);

        displayParameters(params);

        return params;
    }

    private void prepareFunctionName() throws TaskExecutionException {
        getLogger().quiet("Common parameter [Function Name]: name for both the new function and Java class");

        if (settings != null/* && !settings.isInteractiveMode()*/) {
            assureInputInBatchMode(getFunctionName(),
                    str -> isNotEmpty(str) && str.matches(FUNCTION_NAME_REGEXP),
                    this::setFunctionName,
                    true);
        } else {
            assureInputFromUser("Enter value for Function Name: ",
                    getFunctionName(),
                    str -> isNotEmpty(str) && str.matches(FUNCTION_NAME_REGEXP),
                    "Function name must start with a letter and can contain letters, digits, '_' and '-'",
                    this::setFunctionName);
        }
    }

    private void preparePackageName() throws TaskExecutionException {
        getLogger().quiet("Common parameter [Package Name]: package name of the new Java class");

        if (settings != null/* && !settings.isInteractiveMode()*/) {
            assureInputInBatchMode(getFunctionPackageName(),
                    str -> isNotEmpty(str) && isName(str),
                    this::setFunctionPackageName,
                    true);
        } else {
            assureInputFromUser("Enter value for Package Name: ",
                    getFunctionPackageName(),
                    str -> isNotEmpty(str) && isName(str),
                    "Input should be a valid Java package name.",
                    this::setFunctionPackageName);
        }
    }

    private Map<String, String> prepareTemplateParameters(final FunctionTemplate template,
                                                          final Map<String, String> params)
            throws TaskExecutionException {
        for (final String property : template.getMetadata().getUserPrompt()) {
            getLogger().quiet(format("Trigger specific parameter [%s]", property));

            final List<String> options = getOptionsForUserPrompt(property);
            if (settings != null/* && !settings.isInteractiveMode()*/) {
                String initValue = System.getProperty(property);
                if (options != null && options.size() > 0) {
                    final String foundElement = findElementInOptions(options, initValue);
                    initValue = foundElement == null ? options.get(0) : foundElement;
                }

                assureInputInBatchMode(
                        initValue,
                        StringUtils::isNotEmpty,
                        str -> params.put(property, str),
                        false
                );
            } else {
                if (options == null) {
                    assureInputFromUser(
                            format("Enter value for %s: ", property),
                            System.getProperty(property),
                            StringUtils::isNotEmpty,
                            "Input should be a non-empty string.",
                            str -> params.put(property, str)
                    );
                } else {
                    assureInputFromUser(
                            format("Enter value for %s: ", property),
                            System.getProperty(property),
                            options,
                            str -> params.put(property, str)
                    );
                }
            }
        }

        return params;
    }

    private void displayParameters(final Map<String, String> params) {
        getLogger().quiet("Summary of parameters for function template:");

        params.entrySet()
                .stream()
                .forEach(e -> getLogger().quiet(format("%s: %s", e.getKey(), e.getValue())));
    }

    private String substituteParametersInTemplate(final FunctionTemplate template, final Map<String, String> params) {
        String ret = template.getFiles().get("function.java");
        for (final Map.Entry<String, String> entry : params.entrySet()) {
            ret = ret.replace(String.format("$%s$", entry.getKey()), entry.getValue());
        }
        return ret;
    }

    private void saveNewFunctionToFile(final String newFunctionClass) throws Exception {
        getLogger().quiet(SAVE_FILE);

        final File packageDir = getPackageDir();

        final File targetFile = getTargetFile(packageDir);

        createPackageDirIfNotExist(packageDir);

        saveToTargetFile(targetFile, newFunctionClass);

        getLogger().quiet(SAVE_FILE_DONE + targetFile.getAbsolutePath());
    }

    private File getPackageDir() {
        final String sourceRoot = getSourceRoot();
        final String[] packageName = getFunctionPackageName().split("\\.");
        return Paths.get(sourceRoot, packageName).toFile();
    }

    private File getTargetFile(final File packageDir) throws Exception {
        final String functionName = getClassName() + ".java";
        final File targetFile = new File(packageDir, functionName);
        if (targetFile.exists()) {
            throw new FileAlreadyExistsException(format(FILE_EXIST, targetFile.getAbsolutePath()));
        }
        return targetFile;
    }

    private void createPackageDirIfNotExist(final File packageDir) {
        if (!packageDir.exists()) {
            packageDir.mkdirs();
        }
    }

    private void saveToTargetFile(final File targetFile, final String newFunctionClass) throws Exception {
        try (final OutputStream os = new FileOutputStream(targetFile)) {
            copy(newFunctionClass, os);
        }
    }

    private void assureInputFromUser(final String prompt, final String initValue, final List<String> options,
                                     final Consumer<String> setter) {
        final String option = findElementInOptions(options, initValue);
        if (option != null) {
            getLogger().quiet(FOUND_VALID_VALUE);
            setter.accept(option);
            return;
        }

        out.printf("Choose from below options as %s.%n", prompt);
        for (int i = 0; i < options.size(); i++) {
            out.printf("%d. %s%n", i, options.get(i));
        }

        assureInputFromUser("Enter index to use: ",
                null,
                str -> {
                    try {
                        final int index = Integer.parseInt(str);
                        return 0 <= index && index < options.size();
                    } catch (Exception e) {
                        return false;
                    }
                },
                "Invalid index.",
                str -> {
                    final int index = Integer.parseInt(str);
                    setter.accept(options.get(index));
                });
    }

    private void assureInputFromUser(final String prompt, final String initValue,
                                     final Function<String, Boolean> validator, final String errorMessage,
                                     final Consumer<String> setter) {
        if (validator.apply(initValue)) {
            getLogger().quiet(FOUND_VALID_VALUE);
            setter.accept(initValue);
            return;
        }

        final Scanner scanner = getScanner();

        while (true) {
            out.printf(prompt);
            out.flush();
            try {
                final String input = scanner.nextLine();
                if (validator.apply(input)) {
                    setter.accept(input);
                    break;
                }
            } catch (Exception ignored) {
            }
            // Reaching here means invalid input
            getLogger().quiet(errorMessage);
        }
    }

    private void assureInputInBatchMode(final String input, final Function<String, Boolean> validator,
                                        final Consumer<String> setter, final boolean required)
            throws TaskExecutionException {
        if (validator.apply(input)) {
            getLogger().quiet(FOUND_VALID_VALUE);
            setter.accept(input);
            return;
        }

        if (required) {
            throw new IllegalArgumentException(String.format("invalid input: %s", input));
        } else {
            out.printf("The input is invalid. Use empty string.%n");
            setter.accept("");
        }
    }

    private Scanner getScanner() {
        return new Scanner(System.in, "UTF-8");
    }

    private String findElementInOptions(List<String> options, String item) {
        return options.stream()
                .filter(o -> o != null && o.equalsIgnoreCase(item))
                .findFirst()
                .orElse(null);
    }

    private List<String> getOptionsForUserPrompt(final String promptName) {
        if ("authlevel".equalsIgnoreCase(promptName.trim())) {
            return Arrays.asList("ANONYMOUS", "FUNCTION", "ADMIN");
        }
        return null;
    }

}
