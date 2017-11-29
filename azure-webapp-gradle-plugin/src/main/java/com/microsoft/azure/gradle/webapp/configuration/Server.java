/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.gradle.webapp.configuration;

import java.util.HashMap;
import java.util.Map;

public class Server {
    private String id;
    private String username;
    private String password;
    private Map<String, String> configuration = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void addProperty(String key, String value) {
        configuration.put(key, value);
    }

    public String getProperty(String key) {
        return configuration.get(key);
    }
}
