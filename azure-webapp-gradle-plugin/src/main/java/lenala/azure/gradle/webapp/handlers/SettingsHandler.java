package lenala.azure.gradle.webapp.handlers;

import com.microsoft.azure.management.appservice.WebApp.DefinitionStages.WithCreate;
import com.microsoft.azure.management.appservice.WebApp.Update;
import org.gradle.api.GradleException;

public interface SettingsHandler {
    void processSettings(final WithCreate withCreate) throws GradleException;

    void processSettings(final Update update) throws GradleException;
}
