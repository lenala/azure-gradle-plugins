package com.lenala.azure.gradle.webapp.handlers;

import com.google.common.io.Files;
import com.lenala.azure.gradle.webapp.DeployTask;
import com.lenala.azure.gradle.webapp.configuration.AppServiceType;
import com.lenala.azure.gradle.webapp.configuration.DeployTarget;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.GradleException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

/**
 * Artifact handler for deploying a JAR, self-contained, Java application (e.g.
 * Spring Boot) to Azure App Service through FTP
 *
 * @since 1.3.0
 */
public final class JarArtifactHandlerImpl extends ZipArtifactHandlerImpl {
    private static final String FILE_IS_NOT_JAR = "The deployment file is not a jar typed file.";
    private static final String FIND_JAR_FILE_FAIL = "Failed to find the jar file: '%s'";

    private static final String DEFAULT_LINUX_JAR_NAME = "app.jar";
    private static final String JAR_CMD = ":JAR_COMMAND:";
    private static final String FILENAME = ":FILENAME:";
    private static final String DEFAULT_JAR_COMMAND = "-Djava.net.preferIPv4Stack=true " +
                                                             "-Dserver.port=%HTTP_PLATFORM_PORT% " +
                                                             "-jar &quot;%HOME%\\\\site\\\\wwwroot\\\\:FILENAME:&quot;";
    private static final String GENERATE_WEB_CONFIG_FAIL = "Failed to generate web.config file for JAR deployment.";
    private static final String READ_WEB_CONFIG_TEMPLATE_FAIL = "Failed to read the content of web.config.template.";
    private static final String GENERATING_WEB_CONFIG = "Generating web.config for Web App on Windows.";

    protected JarArtifactHandlerImpl(final DeployTask task) {
        super(task);
    }

    @Override
    public void publish(DeployTarget deployTarget) throws IOException {
        final File jar = getJarFile();
        assureJarFileExisted(jar);

        prepareDeploymentFiles(jar);

        super.publish(deployTarget);
    }

    protected void prepareDeploymentFiles(File jar) throws IOException {
        final File parent = new File(getDeploymentStagingDirectoryPath());
        parent.mkdirs();

        if (AppServiceType.LINUX.equals(task.getAzureWebAppExtension().getAppService().getType())) {
            Files.copy(jar, new File(parent, DEFAULT_LINUX_JAR_NAME));
        } else {
            Files.copy(jar, new File(parent, jar.getName()));
            generateWebConfigFile(jar.getName());
        }
    }

    protected void generateWebConfigFile(String jarFileName) throws IOException {
        task.getLogger().quiet(GENERATING_WEB_CONFIG);
        final String templateContent;
        try (final InputStream is = getClass().getResourceAsStream("web.config.template")) {
            templateContent = IOUtils.toString(is, "UTF-8");
        } catch (IOException e) {
            task.getLogger().quiet(READ_WEB_CONFIG_TEMPLATE_FAIL);
            throw e;
        }

        final String webConfigFile = templateContent
                                             .replaceAll(JAR_CMD, DEFAULT_JAR_COMMAND.replaceAll(FILENAME, jarFileName));

        final File webConfig = new File(getDeploymentStagingDirectoryPath(), "web.config");
        webConfig.createNewFile();

        try (final FileOutputStream fos = new FileOutputStream(webConfig)) {
            IOUtils.write(webConfigFile, fos, "UTF-8");
        } catch (Exception e) {
            task.getLogger().quiet(GENERATE_WEB_CONFIG_FAIL);
            throw e;
        }
    }

    protected File getJarFile() {
        String jarFile = task.getAzureWebAppExtension().getDeployment().getJarFile();
        if (StringUtils.isEmpty(jarFile)) {
            jarFile = task.getProject().getTasks().getByPath("bootJar").getOutputs().getFiles().getAsPath();
        }
        return new File(jarFile);
    }

    protected void assureJarFileExisted(File jar) throws GradleException {
        if (!Files.getFileExtension(jar.getName()).equalsIgnoreCase("jar")) {
            throw new GradleException(FILE_IS_NOT_JAR);
        }

        if (!jar.exists() || !jar.isFile()) {
            throw new GradleException(String.format(FIND_JAR_FILE_FAIL, jar.getAbsolutePath()));
        }
    }
}

