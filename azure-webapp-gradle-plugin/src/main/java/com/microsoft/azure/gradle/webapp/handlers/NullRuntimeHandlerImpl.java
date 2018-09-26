/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.gradle.webapp.handlers;

import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.appservice.WebApp.DefinitionStages.WithCreate;
import com.microsoft.azure.management.appservice.WebApp.Update;
import org.gradle.api.GradleException;

import static com.microsoft.azure.gradle.webapp.helpers.CommonStringTemplates.PROPERTY_MISSING_TEMPLATE;

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
