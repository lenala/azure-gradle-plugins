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
    private static final String SERVER_PREFIX = "server.";
    private static Logger logger = Logging.getLogger(Utils.class);

    public static Server getServer(final Project project, String serverId) {
        if (!serverId.equals(project.getProperties().get("server.id"))) {
            logger.error("Please use same server id as defined in gradle.properties");
            return null;
        }
        Server server = new Server();
        server.setId(serverId);
        server.setUsername((String) project.getProperties().get(SERVER_PREFIX + "username"));
        server.setPassword((String) project.getProperties().get(SERVER_PREFIX + "password"));
        for (String key : project.getProperties().keySet()) {
            if (key.toLowerCase().startsWith(SERVER_PREFIX)) {
                server.addProperty(key.substring(SERVER_PREFIX.length()), (String) project.getProperties().get(key));
            }
        }
        return server;
    }

    /**
     * Get string value from server configuration in gradle.properties.
     *
     * @param server Server object.
     * @param key    Key string.
     * @return String value if key exists; otherwise, return null.
     */
    public static String getValueFromServerConfiguration(final Server server, final String key) {
        if (server == null) {
            return null;
        }
        return server.getProperty(key);
    }
}
