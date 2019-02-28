package lenala.azure.gradle.webapp.handlers;

import lenala.azure.gradle.webapp.DeployTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

public abstract class HandlerFactory {
    private static HandlerFactory instance = new HandlerFactoryImpl();

    public static HandlerFactory getInstance() {
        return instance;
    }

    public abstract RuntimeHandler getRuntimeHandler(final DeployTask task) throws GradleException;

    public abstract SettingsHandler getSettingsHandler(final Project project) throws GradleException;

    public abstract ArtifactHandler getArtifactHandler(final DeployTask task) throws GradleException;
}
