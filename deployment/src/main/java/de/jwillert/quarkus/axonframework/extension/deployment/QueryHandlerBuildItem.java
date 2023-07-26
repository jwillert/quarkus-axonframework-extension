package de.jwillert.quarkus.axonframework.extension.deployment;

public final class QueryHandlerBuildItem extends AxonBuildItem {
    public QueryHandlerBuildItem(Class<?> axonAnnotatedClass) {
        super(axonAnnotatedClass);
    }
}
