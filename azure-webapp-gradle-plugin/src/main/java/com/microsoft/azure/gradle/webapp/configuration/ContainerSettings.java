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

    @Input
    private boolean useBuiltInImage;

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

    /**
     * Private registry URL.
     */
    @Input
    @Optional
    private String registryUrl;

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public boolean isUseBuiltInImage() {
        return useBuiltInImage;
    }

    public void setUseBuiltInImage(boolean useBuiltInImage) {
        this.useBuiltInImage = useBuiltInImage;
    }

    public String getStartUpFile() {
        return startUpFile;
    }

    public void setStartUpFile(String startUpFile) {
        this.startUpFile = startUpFile;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getRegistryUrl() {
        return registryUrl;
    }


    public boolean isEmpty() {
        return StringUtils.isEmpty(getImageName()) &&
                StringUtils.isEmpty(getStartUpFile()) &&
                StringUtils.isEmpty(getServerId()) &&
                StringUtils.isEmpty(getRegistryUrl());
    }
}
