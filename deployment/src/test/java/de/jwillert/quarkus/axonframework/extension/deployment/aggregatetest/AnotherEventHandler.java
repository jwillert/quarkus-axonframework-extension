package de.jwillert.quarkus.axonframework.extension.deployment.aggregatetest;

import org.axonframework.eventhandling.EventHandler;

public class AnotherEventHandler {

    @EventHandler
    public void on(TestItemCreatedEvent testItemCreatedEvent) {
        System.out.println("Also the other event handler receives the event: " + testItemCreatedEvent);
    }
}
