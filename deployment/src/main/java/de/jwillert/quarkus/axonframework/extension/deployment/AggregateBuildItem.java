package de.jwillert.quarkus.axonframework.extension.deployment;

public final class AggregateBuildItem extends AxonBuildItem {
    public AggregateBuildItem(Class<?> axonAnnotatedClass) {
        super(axonAnnotatedClass);
    }
}
