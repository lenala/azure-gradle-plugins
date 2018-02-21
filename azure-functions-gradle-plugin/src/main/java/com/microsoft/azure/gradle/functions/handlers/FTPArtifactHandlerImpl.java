/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.gradle.functions.handlers;

import com.microsoft.azure.gradle.functions.FunctionsTask;
import com.microsoft.azure.gradle.functions.helpers.FTPUploader;
import com.microsoft.azure.management.appservice.FunctionApp;
import com.microsoft.azure.management.appservice.PublishingProfile;

public class FTPArtifactHandlerImpl implements ArtifactHandler {
    private static final String DEFAULT_FUNCTION_ROOT = "/site/wwwroot";
    private static final int DEFAULT_MAX_RETRY_TIMES = 3;

    private FunctionsTask functionsTask;

    public FTPArtifactHandlerImpl(final FunctionsTask functionsTask) {
        this.functionsTask = functionsTask;
    }

    @Override
    public void publish() throws Exception {
        final FTPUploader uploader = getUploader();
        final FunctionApp app = functionsTask.getFunctionApp();
        final PublishingProfile profile = app.getPublishingProfile();
        final String serverUrl = profile.ftpUrl().split("/", 2)[0];

        uploader.uploadDirectoryWithRetries(
                serverUrl,
                profile.ftpUsername(),
                profile.ftpPassword(),
                functionsTask.getDeploymentStageDirectory(),
                DEFAULT_FUNCTION_ROOT,
                DEFAULT_MAX_RETRY_TIMES);

        app.syncTriggers();
    }

    protected FTPUploader getUploader() {
        return new FTPUploader(functionsTask.getLogger());
    }
}
