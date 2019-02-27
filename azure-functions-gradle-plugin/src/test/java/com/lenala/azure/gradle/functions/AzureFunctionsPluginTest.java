package com.lenala.azure.gradle.functions;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

@Ignore("AzureFunctionsPlugin does not add a single task when applied to a project")
public class AzureFunctionsPluginTest {
    @Test
    public void greeterPluginAddsGreetingTaskToProject() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("com.lenala.azure.azurefunctions");

        assertTrue(project.getTasks().findByPath("package") instanceof PackageTask);
    }
}
