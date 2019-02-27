package com.lenala.azure.gradle.webapp.auth;

import com.lenala.azure.gradle.webapp.configuration.Authentication;

public interface AuthConfiguration {

    String getSubscriptionId();

    String getUserAgent();

    Authentication getAuthenticationSettings();
}
