/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.gradle.functions.auth;

public interface AuthConfiguration {

    String getSubscriptionId();

    String getUserAgent();

    boolean hasAuthenticationSettings();

    String getAuthenticationSetting(String key);

    String getAuthFile();
}
