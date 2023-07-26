package de.jwillert.quarkus.axonframework.extension.deployment;

public final class CommandHandlerBuildItem extends AxonBuildItem {
    public CommandHandlerBuildItem(Class<?> axonAnnotatedClass) {
        super(axonAnnotatedClass);
    }
}
