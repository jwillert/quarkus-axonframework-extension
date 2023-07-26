package de.jwillert.quarkus.axonframework.extension.it.command.api.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

public class CreateShopingListCommand {

    @TargetAggregateIdentifier
    UUID id;

    public CreateShopingListCommand(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
