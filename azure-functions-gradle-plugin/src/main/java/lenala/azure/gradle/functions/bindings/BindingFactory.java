package lenala.azure.gradle.functions.bindings;

import com.microsoft.azure.functions.annotation.CustomBinding;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Locale;

public class BindingFactory {
    private static final String HTTP_OUTPUT_DEFAULT_NAME = "$return";

    public static Binding getBinding(final Annotation annotation) {
        final BindingEnum annotationEnum = Arrays.stream(BindingEnum.values())
            .filter(bindingEnum -> bindingEnum.name().toLowerCase(Locale.ENGLISH)
                .equals(annotation.annotationType().getSimpleName().toLowerCase(Locale.ENGLISH)))
            .findFirst().orElse(null);
        return annotationEnum == null ? getUserDefinedBinding(annotation) : new Binding(annotationEnum, annotation);
    }

    public static Binding getUserDefinedBinding(final Annotation annotation) {
        final CustomBinding customBindingAnnotation = annotation.annotationType()
            .getDeclaredAnnotation(com.microsoft.azure.functions.annotation.CustomBinding.class);
        return customBindingAnnotation == null ? null : new ExtendedCustomBinding(BindingEnum.ExtendedCustomBinding,
                customBindingAnnotation, annotation);
    }

    public static Binding getHTTPOutBinding() {
        final Binding result = new Binding(BindingEnum.HttpOutput);
        result.setName(HTTP_OUTPUT_DEFAULT_NAME);
        return result;
    }
}
