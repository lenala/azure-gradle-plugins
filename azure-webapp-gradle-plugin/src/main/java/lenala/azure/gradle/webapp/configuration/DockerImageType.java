package lenala.azure.gradle.webapp.configuration;

public enum DockerImageType {
    NONE,
    PUBLIC_DOCKER_HUB,
    PRIVATE_DOCKER_HUB,
    PRIVATE_REGISTRY,
    UNKNOWN,
    BUILT_IN
}
