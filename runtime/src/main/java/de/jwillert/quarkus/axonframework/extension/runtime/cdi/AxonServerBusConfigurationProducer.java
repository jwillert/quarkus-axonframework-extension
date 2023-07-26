package de.jwillert.quarkus.axonframework.extension.runtime.cdi;

import de.jwillert.quarkus.axonframework.extension.runtime.cdi.qualifier.AxonSerializer;
import de.jwillert.quarkus.axonframework.extension.runtime.cdi.qualifier.LocalSegment;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;
import org.axonframework.axonserver.connector.AxonServerConfiguration;
import org.axonframework.axonserver.connector.AxonServerConnectionManager;
import org.axonframework.axonserver.connector.TargetContextResolver;
import org.axonframework.axonserver.connector.command.AxonServerCommandBus;
import org.axonframework.axonserver.connector.command.CommandLoadFactorProvider;
import org.axonframework.axonserver.connector.command.CommandPriorityCalculator;
import org.axonframework.axonserver.connector.event.axon.AxonServerEventStore;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.distributed.RoutingStrategy;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.serialization.Serializer;
import org.axonframework.tracing.SpanFactory;

@ApplicationScoped
@IfBuildProperty(name = "axon.axonserver.enabled", stringValue = "true", enableIfMissing = true)
public class AxonServerBusConfigurationProducer {

    @Produces
    @Default
    @Dependent
    public CommandBus axonServerCommandBus(AxonServerConnectionManager axonServerConnectionManager,
                                           AxonServerConfiguration axonServerConfiguration,
                                           @LocalSegment CommandBus localSegment,
                                           @AxonSerializer(type = AxonSerializer.SerializerType.MESSAGE) Serializer messageSerializer,
                                           RoutingStrategy routingStrategy,
                                           CommandPriorityCalculator priorityCalculator,
                                           CommandLoadFactorProvider loadFactorProvider,
                                           TargetContextResolver targetContextResolver,
                                           SpanFactory spanFactory) {
        return AxonServerCommandBus.builder()
                .axonServerConnectionManager(axonServerConnectionManager)
                .configuration(axonServerConfiguration)
                .localSegment(SimpleCommandBus.builder().build())
                .serializer(messageSerializer)
                .routingStrategy(routingStrategy)
//                .priorityCalculator(priorityCalculator)
//                .loadFactorProvider(loadFactorProvider)
                .targetContextResolver(targetContextResolver)
//                .spanFactory(spanFactory)
                .build();
    }

    @Produces
    @Dependent
    public EventStore eventStore(AxonServerConfiguration axonServerConfiguration,
                                 org.axonframework.config.Configuration configuration,
                                 AxonServerConnectionManager axonServerConnectionManager,
                                 Serializer snapshotSerializer,
                                 @AxonSerializer(type = AxonSerializer.SerializerType.EVENT) Serializer eventSerializer) {
        return AxonServerEventStore.builder()
//                .messageMonitor(configuration
//                        .messageMonitor(AxonServerEventStore.class, "eventStore"))
                .configuration(axonServerConfiguration)
                .platformConnectionManager(axonServerConnectionManager)
                .snapshotSerializer(snapshotSerializer)
                .eventSerializer(eventSerializer)
//                .snapshotFilter(configuration.snapshotFilter())
//                .upcasterChain(configuration.upcasterChain())
//                .spanFactory(configuration.spanFactory())
                .build();
    }

}

