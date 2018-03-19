/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.gradle.webapp.configuration;


import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

public class AppServiceOnLinux {
    /**
     * Docker image name
     */
    @Input
    private String runtimeStack;

    @Input
    @Optional
    private String urlPath;

    public String getRuntimeStack() {
        return runtimeStack;
    }

    public String getUrlPath() {
        return urlPath;
    }
}
