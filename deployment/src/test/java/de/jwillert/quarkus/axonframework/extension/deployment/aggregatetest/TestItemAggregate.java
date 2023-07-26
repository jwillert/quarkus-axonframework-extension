package de.jwillert.quarkus.axonframework.extension.deployment.aggregatetest;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import de.jwillert.quarkus.axonframework.extension.runtime.stereotype.Aggregate;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;

@Aggregate
public class TestItemAggregate {

    @AggregateIdentifier
    private String id;

    public TestItemAggregate() {
    }

    @CommandHandler
    public TestItemAggregate(CreateTestItemCommand createTestItemCommand, TestService testService) {
        System.out.println("Command received: " + createTestItemCommand);

        // Testservice is a bean injected into the method.
        testService.doSomethingToTestDIInjection();

        apply(new TestItemCreatedEvent(createTestItemCommand.getId(), createTestItemCommand.getCustomerId(),
                createTestItemCommand.getBalance()));
    }

    @EventSourcingHandler
    public void on(TestItemCreatedEvent testItemCreatedEvent) {
        System.out.println("Test event received: " + testItemCreatedEvent);
        id = testItemCreatedEvent.getId();
    }
}
