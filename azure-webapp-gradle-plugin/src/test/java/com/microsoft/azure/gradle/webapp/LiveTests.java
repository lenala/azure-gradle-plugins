/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.gradle.webapp;

import org.gradle.api.tasks.Input;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.assertEquals;

public class LiveTests {
    @Rule public final TemporaryFolder testProjectDir = new TemporaryFolder();
//    File projectDir = new File(".\\testProjects\\simpleProject");
    private File projectDir = new File(".\\testProjects\\dockerProject");

    @Ignore
    @Test
    public void testDeployTask() throws IOException {
        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir)
                .forwardOutput()
                .withArguments(DeployTask.TASK_NAME)
                .withPluginClasspath()
                .build();
        assertEquals(result.task(":" + DeployTask.TASK_NAME).getOutcome(), SUCCESS);
    }

    @After
    public void afterTest() {

    }

}
