package lenala.azure.gradle.webapp.configuration;

import com.microsoft.azure.management.appservice.JavaVersion;
import com.microsoft.azure.management.appservice.WebContainer;
import org.apache.commons.lang3.StringUtils;

public class AppService {
    private AppServiceType type;
    private String runtimeStack;
    private String javaWebContainer;
    private String javaVersion;
    private String imageName;
    private String startUpFile;
    private String serverId;
    private String username;
    private String password;
    private String registryUrl;

    public String getRuntimeStack() {
        return this.runtimeStack;
    }

    public AppServiceType getType() {
        return this.type;
    }

    public WebContainer getJavaWebContainer() {
        return StringUtils.isEmpty(javaWebContainer)
                ? WebContainer.TOMCAT_8_5_NEWEST
                : WebContainer.fromString(javaWebContainer);
    }

    public JavaVersion getJavaVersion() {
        return StringUtils.isEmpty(javaVersion) ? null : JavaVersion.fromString(javaVersion);
    }

    public String getImageName() {
        return this.imageName;
    }

    public String getStartUpFile() {
        return startUpFile;
    }

    public String getServerId() {
        return serverId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRegistryUrl() {
        return this.registryUrl;
    }
}
