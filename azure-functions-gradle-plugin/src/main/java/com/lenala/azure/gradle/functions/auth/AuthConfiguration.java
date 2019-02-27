package com.lenala.azure.gradle.functions.auth;

public interface AuthConfiguration {

    String getSubscriptionId();

    String getUserAgent();

    boolean hasAuthenticationSettings();

    String getAuthenticationSetting(String key);

    String getAuthFile();
}
