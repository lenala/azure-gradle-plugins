/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.gradle.webapp.handlers;

import com.microsoft.azure.gradle.webapp.DeployTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

public abstract class HandlerFactory {
    private static HandlerFactory instance = new HandlerFactoryImpl();

    public static HandlerFactory getInstance() {
        return instance;
    }

    public abstract RuntimeHandler getRuntimeHandler(final DeployTask task) throws GradleException;

    public abstract SettingsHandler getSettingsHandler(final Project project) throws GradleException;

    public abstract ArtifactHandler getArtifactHandler(final DeployTask task) throws GradleException;
}
