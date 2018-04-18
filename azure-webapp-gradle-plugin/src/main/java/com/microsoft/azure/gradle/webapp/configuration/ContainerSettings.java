/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.gradle.webapp.configuration;

import org.apache.commons.lang3.StringUtils;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

/**
 * Docker container setting class.
 */
public class ContainerSettings {
    /**
     * Docker image name
     */
    @Input
    private String imageName;

    /**
     * Start up file.
     */
    @Input
    @Optional
    private String startUpFile;

    /**
     * Server Id.
     */
    @Input
    @Optional
    private String serverId;

    @Input
    @Optional
    private String username;

    @Input
    @Optional
    private String password;

    /**
     * Private registry URL.
     */
    @Input
    @Optional
    private String registryUrl;

    public String getImageName() {
        return imageName;
    }

    public String getStartUpFile() {
        return startUpFile;
    }

    public String getServerId() {
        return serverId;
    }

    public String getRegistryUrl() {
        return registryUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isEmpty() {
        return StringUtils.isEmpty(getImageName()) &&
                StringUtils.isEmpty(getStartUpFile()) &&
                StringUtils.isEmpty(getServerId()) &&
                StringUtils.isEmpty(getRegistryUrl());
    }
}
