package lenala.azure.gradle.webapp.handlers;

import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.appservice.WebApp.DefinitionStages.WithCreate;
import com.microsoft.azure.management.appservice.WebApp.Update;

public interface RuntimeHandler {
    WithCreate defineAppWithRuntime() throws Exception;

    Update updateAppRuntime(final WebApp app) throws Exception;
}
