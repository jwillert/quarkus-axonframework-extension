package de.jwillert.quarkus.axonframework.extension.it.command.api.events;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

public class ShoppingListDeletedEvent {

    @TargetAggregateIdentifier
    UUID id;

    public ShoppingListDeletedEvent(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
