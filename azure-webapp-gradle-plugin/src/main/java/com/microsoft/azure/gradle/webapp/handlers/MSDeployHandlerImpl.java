/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.gradle.webapp.handlers;

import com.microsoft.azure.gradle.webapp.DeployTask;

public class MSDeployHandlerImpl implements ArtifactHandler {
    private DeployTask task;

    public MSDeployHandlerImpl(final DeployTask task) {
        this.task = task;
    }

    @Override
    public void publish() throws Exception {
//        copyResourceToStageDirectory();

        task.getWebApp().deploy()
                .withPackageUri(task.getAzureWebAppExtension().getPackageUri())
                .withExistingDeploymentsDeleted(false)
                .execute();

    }

//    protected void copyResourceToStageDirectory() throws IOException {
//        String target = azureWebAppExtension.getTarget();
////        Files.copy(Paths.get(target), Paths.get(task.getDeploymentStageDirectory()));
//        FileUtils.copyFileToDirectory(new File(target), new File(getDeploymentStageDirectory()));
////        Utils.copyResources(/*mojo.getProject()*/null,
////                task.getSession(),
////                mojo.getMavenResourcesFiltering(),
////                resources,
////                mojo.getDeploymentStageDirectory());
//    }

//    protected void copyResourcesToStageDirectory(final List<Resource> resources) throws IOException {

//        Utils.copyResources(/*mojo.getProject()*/null,
//                task.getSession(),
//                mojo.getMavenResourcesFiltering(),
//                resources,
//                mojo.getDeploymentStageDirectory());
//    }

//    private String getDeploymentStageDirectory() {
//        String stageDirectory = Paths.get(getBuildDirectoryAbsolutePath(),
//                "azure-webapps",
//                azureWebAppExtension.getAppName()).toString();
//        logger.quiet(stageDirectory);
//        return stageDirectory;
//    }
//
//    public String getBuildDirectoryAbsolutePath() {
//        return project.getBuildDir().getAbsolutePath();
//    }
}
