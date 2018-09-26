/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.gradle.webapp.configuration;

public class Authentication {
    public AuthenticationType type;
    public String file;
    public String client;
    public String tenant;
    public String key;
    public String certificate;
    public String certificatePassword;
    public String environment = "AZURE";


    public String getFile() {
        return file;
    }

    public String getClient() {
        return client;
    }

    public String getTenant() {
        return tenant;
    }

    public String getKey() {
        return key;
    }

    public String getEnvironment() {
        return environment;
    }

    public AuthenticationType getType() {
        return type;
    }

    public String getCertificate() {
        return certificate;
    }

    public String getCertificatePassword() {
        return certificatePassword;
    }
}
