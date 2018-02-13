/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.gradle.functions;

import groovy.lang.Closure;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Project;
import org.gradle.api.tasks.Input;

public class AzureFunctionsExtension {
    @Input
    private String appName;
    @Input
    private String resourceGroup;
    @Input
    String region = "westus2";
    @Input
    private String subscriptionId;
    @Input
    private String authFile;

    private Project project;

    public AzureFunctionsExtension(Project project) {
        this.project = project;
    }

    public String getAppName() {
        return appName;
    }

    public String getResourceGroup() {
        return resourceGroup;
    }

    public String getRegion() {
        return region;
    }

    public String getAuthFile() {
        return authFile;
    }
}
