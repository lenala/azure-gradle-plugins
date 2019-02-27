package com.lenala.azure.gradle.webapp.handlers;

import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.appservice.WebApp.DefinitionStages.WithCreate;
import com.microsoft.azure.management.appservice.WebApp.Update;
import org.gradle.api.GradleException;

import static com.lenala.azure.gradle.webapp.helpers.CommonStringTemplates.PROPERTY_MISSING_TEMPLATE;

public class NullRuntimeHandlerImpl implements RuntimeHandler {
    @Override
    public WithCreate defineAppWithRuntime() throws Exception {
        throw new GradleException(String.format(PROPERTY_MISSING_TEMPLATE, "appService"));
    }

    @Override
    public Update updateAppRuntime(final WebApp app) throws Exception {
        return app.update();
    }
}
