package de.jwillert.quarkus.axonframework.extension.it.command;

import de.jwillert.quarkus.axonframework.extension.it.command.api.command.CreateShopingListCommand;
import de.jwillert.quarkus.axonframework.extension.it.command.api.command.DeleteShopingListCommand;
import de.jwillert.quarkus.axonframework.extension.it.command.api.events.ShoppingListCreatedEvent;
import de.jwillert.quarkus.axonframework.extension.it.command.api.events.ShoppingListDeletedEvent;
import de.jwillert.quarkus.axonframework.extension.runtime.stereotype.Aggregate;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateCreationPolicy;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.CreationPolicy;

import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;
import static org.axonframework.modelling.command.AggregateLifecycle.markDeleted;

@Aggregate
public class ShoppingList {

    @AggregateIdentifier
    UUID shoppingListId;

    public ShoppingList() {
    }

    @CommandHandler
    @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
    public void onCreateCreateShopingList(CreateShopingListCommand command) {
        apply(new ShoppingListCreatedEvent(command.getId()));
    }

    @EventSourcingHandler
    public void onCreated(ShoppingListCreatedEvent event) {
        shoppingListId = event.getId();
    }

    @CommandHandler
    public void onDeleteShopingList(DeleteShopingListCommand command) {
        shoppingListId = command.getId();
        apply(new ShoppingListDeletedEvent(command.getId()));
    }

    @EventSourcingHandler
    public void onDeleted(ShoppingListDeletedEvent event) {
        markDeleted();
    }

}
