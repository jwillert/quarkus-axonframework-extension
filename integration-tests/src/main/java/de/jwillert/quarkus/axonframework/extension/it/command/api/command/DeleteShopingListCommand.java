package de.jwillert.quarkus.axonframework.extension.it.command.api.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

public class DeleteShopingListCommand {

    @TargetAggregateIdentifier
    UUID id;

    public DeleteShopingListCommand(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
