package lenala.azure.gradle.functions.handlers;


import lenala.azure.gradle.functions.configuration.FunctionConfiguration;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AnnotationHandler {
    Set<Method> findFunctions(final List<URL> urls);

    Map<String, FunctionConfiguration> generateConfigurations(final Set<Method> methods) throws Exception;

    FunctionConfiguration generateConfiguration(final Method method) throws Exception;
}
