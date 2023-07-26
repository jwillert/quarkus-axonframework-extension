package de.jwillert.quarkus.axonframework.extension.deployment;

public final class BasePackageBuildItem extends AxonBuildItem {
    public BasePackageBuildItem(Class<?> axonAnnotatedClass) {
        super(axonAnnotatedClass);
    }
}
