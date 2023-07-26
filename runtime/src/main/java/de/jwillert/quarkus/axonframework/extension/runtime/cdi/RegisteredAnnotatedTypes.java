package de.jwillert.quarkus.axonframework.extension.runtime.cdi;

import de.jwillert.quarkus.axonframework.extension.runtime.AxonRuntimeConfig;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class RegisteredAnnotatedTypes {

    private AxonRuntimeConfig axonRuntimeConfig;

    private List<Object> sagas = new ArrayList<>();
    private List<Object> aggregates = new ArrayList<>();
    private List<Object> commandHandlers = new ArrayList<>();
    private List<Object> eventHandlers = new ArrayList<>();
    private List<Object> queryHandlers = new ArrayList<>();
    private List<String> basePackage;

    public void setAxonRuntimeConfig(AxonRuntimeConfig axonRuntimeConfig) {
        this.axonRuntimeConfig = axonRuntimeConfig;
    }

    public List<String> getBasePackage() {
        return basePackage;
    }

    public AxonRuntimeConfig getAxonRuntimeConfig() {
        return axonRuntimeConfig;
    }

    public void registerSaga(Object annotatedBean) {
        addIfNotExist(annotatedBean, sagas);
    }

    public void registerAggregate(Object annotatedBean) {
        addIfNotExist(annotatedBean, aggregates);
    }

    public void registerEventHandler(Object annotatedBean) {
        addIfNotExist(annotatedBean, eventHandlers);
    }

    public void registerCommandHandler(Object annotatedBean) {
        addIfNotExist(annotatedBean, commandHandlers);
    }

    public void registerQueryHandler(Object annotatedBean) {
        addIfNotExist(annotatedBean, queryHandlers);
    }

    private void addIfNotExist(Object annotatedBean, List<Object> aggregates) {
        boolean alreadyExist = aggregates.stream().anyMatch(i -> i.getClass().equals(annotatedBean.getClass()));
        if (!alreadyExist) {
            aggregates.add(annotatedBean);
        }
    }

    public List<Object> getSagas() {
        return sagas;
    }

    public List<Object> getAggregates() {
        return aggregates;
    }

    public List<Object> getCommandHandlers() {
        return commandHandlers;
    }

    public List<Object> getEventHandlers() {
        return eventHandlers;
    }

    public List<Object> getQueryHandlers() {
        return queryHandlers;
    }

    public void setBasepackage(List<String> basePackage) {

        this.basePackage = basePackage;
    }
}
