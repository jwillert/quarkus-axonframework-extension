package de.jwillert.quarkus.axonframework.extension.deployment;

public final class SagaBuildItem extends AxonBuildItem {
    public SagaBuildItem(Class<?> axonAnnotatedClass) {
        super(axonAnnotatedClass);
    }
}
