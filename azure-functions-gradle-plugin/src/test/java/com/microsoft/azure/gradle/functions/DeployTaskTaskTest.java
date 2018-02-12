/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.gradle.functions;

import com.microsoft.azure.credentials.AzureCliCredentials;
import com.microsoft.azure.management.Azure;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.assertEquals;

public class DeployTaskTaskTest {
    @Rule public final TemporaryFolder testProjectDir = new TemporaryFolder();
//    File projectDir = new File(".\\testProjects\\simpleProject");
    private File projectDir = new File("testProjects/dockerProject");
    private URL pluginClasspathResource = getClass().getClassLoader().getResource("plugin-classpath.txt");
    private Iterable<File> pluginClassPath;

    @Before
    public void setup() throws IOException {
        pluginClassPath = Files.lines(Paths.get(pluginClasspathResource.getFile())).map(File::new).collect(Collectors.toList());
    }

    @Test
    public void testDeployTask() throws IOException {
        final Azure.Authenticated auth = Azure.configure().authenticate(AzureCliCredentials.create());
        System.out.println(System.getProperty("user.home"));

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir)
                .forwardOutput()
                .withPluginClasspath(pluginClassPath)
                .withArguments("deploy")
                .build();
        assertEquals(result.task(":deploy").getOutcome(), SUCCESS);
    }
}
