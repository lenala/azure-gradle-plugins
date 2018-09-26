/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.gradle.webapp.handlers;

import com.microsoft.azure.gradle.webapp.configuration.DeployTarget;

public interface ArtifactHandler {
    void publish(DeployTarget deployTarget) throws Exception;
}
