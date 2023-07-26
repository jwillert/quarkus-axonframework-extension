package de.jwillert.quarkus.axonframework.extension.deployment;

public final class EventHandlerBuildItem extends AxonBuildItem {
    public EventHandlerBuildItem(Class<?> axonAnnotatedClass) {
        super(axonAnnotatedClass);
    }
}
