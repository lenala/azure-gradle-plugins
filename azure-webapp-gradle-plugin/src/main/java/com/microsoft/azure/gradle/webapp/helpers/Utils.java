/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.gradle.webapp.helpers;

import org.gradle.api.Project;
import com.microsoft.azure.gradle.webapp.configuration.Server;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

/**
 * Utility class
 */
public final class Utils {
    private static Logger logger = Logging.getLogger(Utils.class);

    /**
     * @return conatiner registry information from configuration
     */
    public static Server getServer(final Project project, String serverId) {
        if (!serverId.equals(project.getProperties().get("serverId"))) {
            logger.error("Please use same server id as defined in gradle.properties");
            return null;
        }
        Server server = new Server();
        server.setId(serverId);
        server.setUsername((String) project.getProperties().get("serverUsername"));
        server.setPassword((String) project.getProperties().get("serverPassword"));
        return server;
    }
}
