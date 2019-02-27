package com.lenala.azure.gradle.webapp.helpers;

public final class CommonStringTemplates {
    public static final String PROPERTY_MISSING_TEMPLATE = "'%s' is not configured in build.gradle.";
    public static final String UNKNOWN_VALUE_TEMPLATE = "Unknown value: '%s' in build.gradle";
    public static final String APP_SERVICE_PROPERTY_MISSING_TEMPLATE =
            "'%s' is required in build.gradle when appService.type is '%s'";
    public static final String NOT_COMPATIBLE_WEBAPP_TEMPLATE =
            "azureWebApp configured an existing web app in build.gradle and it is not '%s' Web App. "
                    + "Please align appService configuration with a compatible web app. ";
}
