package de.jwillert.quarkus.axonframework.extension.deployment.sagatest;

public class TestSagaStartEvent {

    private String id;

    public TestSagaStartEvent() {

    }

    public TestSagaStartEvent(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
