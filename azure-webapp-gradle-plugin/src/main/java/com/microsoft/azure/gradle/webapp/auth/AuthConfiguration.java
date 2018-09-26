/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.gradle.webapp.auth;

import com.microsoft.azure.gradle.webapp.configuration.Authentication;

public interface AuthConfiguration {

    String getSubscriptionId();

    String getUserAgent();

    Authentication getAuthenticationSettings();
}
