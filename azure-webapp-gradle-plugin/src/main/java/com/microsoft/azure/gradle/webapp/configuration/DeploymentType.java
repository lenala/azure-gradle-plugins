/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.gradle.webapp.configuration;


import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public enum DeploymentType {
    NONE,
    FTP,
    WEBDEPLOY,
    WARDEPLOY,
    UNKNOWN;

//    public static DeploymentType fromString(final String input) {
//        if (StringUtils.isEmpty(input)) {
//            return NONE;
//        }
//
//        switch (input.toUpperCase(Locale.ENGLISH)) {
//            case "FTP":
//                return FTP;
//            case
//            default:
//                return UNKNOWN;
//        }
//    }
}
