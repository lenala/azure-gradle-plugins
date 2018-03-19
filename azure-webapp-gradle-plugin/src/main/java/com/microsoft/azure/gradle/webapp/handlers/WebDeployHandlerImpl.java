///**
// * Copyright (c) Microsoft Corporation. All rights reserved.
// * Licensed under the MIT License. See License.txt in the project root for
// * license information.
// */
//package com.microsoft.azure.gradle.webapp.handlers;
//
//import com.microsoft.azure.gradle.webapp.DeployTask;
//
//public class WebDeployHandlerImpl implements ArtifactHandler {
//    private DeployTask task;
//
//    public WebDeployHandlerImpl(final DeployTask task) {
//        this.task = task;
//    }
//
//    @Override
//    public void publish() throws Exception {
////        copyResourceToStageDirectory();
//
//        task.getWebApp().deploy()
//                .withPackageUri(task.getAzureWebAppExtension().getPackageUri())
//                .withExistingDeploymentsDeleted(true)
//                .execute();
//
//    }
//}
