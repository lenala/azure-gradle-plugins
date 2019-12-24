package lenala.azure.gradle.functions.handlers;

import com.microsoft.azure.functions.annotation.StorageAccount;
import lenala.azure.gradle.functions.bindings.Binding;
import lenala.azure.gradle.functions.bindings.BindingEnum;
import lenala.azure.gradle.functions.bindings.BindingFactory;
import lenala.azure.gradle.functions.configuration.FunctionConfiguration;
import com.microsoft.azure.functions.annotation.FunctionName;

import org.apache.commons.lang3.StringUtils;
import org.gradle.api.logging.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class AnnotationHandlerImpl implements AnnotationHandler {
    private Logger logger;

    public AnnotationHandlerImpl(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public Set<Method> findFunctions(final List<URL> urls) {
        return new Reflections(
                new ConfigurationBuilder()
                        .addUrls(urls)
                        .setScanners(new MethodAnnotationsScanner())
                        .addClassLoader(getClassLoader(urls)))
                .getMethodsAnnotatedWith(FunctionName.class);
    }

    private ClassLoader getClassLoader(final List<URL> urlList) {
        final URL[] urlArray = urlList.toArray(new URL[urlList.size()]);
        return new URLClassLoader(urlArray, this.getClass().getClassLoader());
    }

    @Override
    public Map<String, FunctionConfiguration> generateConfigurations(final Set<Method> methods) throws Exception {
        final Map<String, FunctionConfiguration> configMap = new HashMap<>();
        for (final Method method : methods) {
            final FunctionName functionAnnotation = method.getAnnotation(FunctionName.class);
            final String functionName = functionAnnotation.value();
            validateFunctionName(configMap.keySet(), functionName);
            logger.quiet("Starting processing function : " + functionName);
            configMap.put(functionName, generateConfiguration(method));
        }
        return configMap;
    }

    private void validateFunctionName(final Set<String> nameSet, final String functionName) throws Exception {
        if (StringUtils.isEmpty(functionName)) {
            throw new Exception("Azure Function name cannot be empty.");
        }
        if (nameSet.stream().anyMatch(n -> StringUtils.equalsIgnoreCase(n, functionName))) {
            throw new Exception("Found duplicate Azure Function: " + functionName);
        }
    }

    @Override
    public FunctionConfiguration generateConfiguration(final Method method) {
        final FunctionConfiguration config = new FunctionConfiguration();
        final List<Binding> bindings = config.getBindings();

        processParameterAnnotations(method, bindings);

        processMethodAnnotations(method, bindings);

        patchStorageBinding(method, bindings);

        config.setEntryPoint(method.getDeclaringClass().getCanonicalName() + "." + method.getName());
        return config;
    }

    private void processParameterAnnotations(final Method method, final List<Binding> bindings) {
        for (final Parameter param : method.getParameters()) {
            bindings.addAll(parseAnnotations(param::getAnnotations, this::parseParameterAnnotation));
        }
    }

    private void processMethodAnnotations(final Method method, final List<Binding> bindings) {
        if (!method.getReturnType().equals(Void.TYPE)) {
            bindings.addAll(parseAnnotations(method::getAnnotations, this::parseMethodAnnotation));

            if (bindings.stream().anyMatch(b -> b.getBindingEnum() == BindingEnum.HttpTrigger) &&
                    bindings.stream().noneMatch(b -> b.getName().equalsIgnoreCase("$return"))) {
                bindings.add(BindingFactory.getHTTPOutBinding());
            }
        }
    }

    private List<Binding> parseAnnotations(Supplier<Annotation[]> annotationProvider,
                                             Function<Annotation, Binding> annotationParser) {
        final List<Binding> bindings = new ArrayList<>();

        for (final Annotation annotation : annotationProvider.get()) {
            final Binding binding = annotationParser.apply(annotation);
            if (binding != null) {
                logger.quiet("Adding binding: " + binding.toString());
                bindings.add(binding);
            }
        }
        return bindings;
    }

    private Binding parseParameterAnnotation(final Annotation annotation) {
        return BindingFactory.getBinding(annotation);
    }

    private Binding parseMethodAnnotation(final Annotation annotation) {
        final Binding ret = parseParameterAnnotation(annotation);
        if (ret != null) {
            ret.setName("$return");
        }
        return ret;
    }

    private void patchStorageBinding(final Method method, final List<Binding> bindings) {
        final Optional<Annotation> storageAccount = Arrays.stream(method.getAnnotations())
                .filter(annotation -> annotation instanceof StorageAccount)
                .findFirst();

        if (storageAccount.isPresent()) {
            logger.quiet("StorageAccount annotation found.");
            final String connectionString = ((StorageAccount) storageAccount.get()).value();
            // Replace empty connection string
            bindings.stream().filter(binding -> binding.getBindingEnum().isStorage())
                    .filter(binding -> StringUtils.isEmpty((String) binding.getAttribute("connection")))
                    .forEach(binding -> binding.setAttribute("connection", connectionString));
        } else {
            logger.quiet("No StorageAccount annotation found.");
        }
    }
}
