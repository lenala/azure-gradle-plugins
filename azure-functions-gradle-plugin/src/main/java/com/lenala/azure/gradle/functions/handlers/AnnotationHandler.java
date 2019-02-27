package com.lenala.azure.gradle.functions.handlers;


import com.lenala.azure.gradle.functions.configuration.FunctionConfiguration;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;
import java.util.Set;

public interface AnnotationHandler {
    Set<Method> findFunctions(final URL url);

    Map<String, FunctionConfiguration> generateConfigurations(final Set<Method> methods) throws Exception;

    FunctionConfiguration generateConfiguration(final Method method) throws Exception;
}
