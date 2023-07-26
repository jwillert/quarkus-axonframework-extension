package de.jwillert.quarkus.axonframework.extension.deployment.sagatest;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import de.jwillert.quarkus.axonframework.extension.runtime.stereotype.Aggregate;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;

@Aggregate
public class TestSagaAggregate {

    @AggregateIdentifier
    private String id;

    public TestSagaAggregate() {
    }

    @CommandHandler
    public TestSagaAggregate(StartSagaFlowCommand cmd) {
        apply(new TestSagaStartEvent(cmd.getId()));
    }

    @EventSourcingHandler
    public void on(TestSagaStartEvent event) {
        id = event.getId();
    }

    @CommandHandler
    public void handle(NextSagaFlowStepCommand cmd) {
        apply(new TestSagaIntermediateEvent(cmd.getId()));
    }

    @CommandHandler
    public void handle(StopSagaFlowCommand cmd) {
        apply(new TestSagaEndEvent(cmd.getId()));
    }
}
