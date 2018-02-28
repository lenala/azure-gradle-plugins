/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.gradle.functions;

import org.gradle.api.tasks.Input;

public class AzureFunctionsExtension {
    @Input
    private String authFile;

    public String getAuthFile() {
        return authFile;
    }
}
