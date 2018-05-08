/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.gradle.webapp.handlers;

import com.microsoft.azure.gradle.webapp.helpers.WebAppUtils;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.appservice.WebApp.DefinitionStages.WithCreate;
import com.microsoft.azure.management.appservice.WebApp.Update;
import org.gradle.api.GradleException;

public class NullRuntimeHandlerImpl implements RuntimeHandler {
    static final String NO_RUNTIME_CONFIG = "No runtime stack is specified in build.gradle; " +
            "use 'appServiceOnWindows', 'appServiceOnLinux' or 'containerSettings' to configure runtime stack.";

    @Override
    public WithCreate defineAppWithRuntime() throws Exception {
        throw new GradleException(NO_RUNTIME_CONFIG);
    }

    @Override
    public Update updateAppRuntime(final WebApp app) throws Exception {
        return app.update();
    }
}
