package de.jwillert.quarkus.axonframework.extension.runtime.cdi;

import de.jwillert.quarkus.axonframework.extension.runtime.AxonRuntimeConfig;
import de.jwillert.quarkus.axonframework.extension.runtime.cdi.qualifier.AxonSerializer;
import de.jwillert.quarkus.axonframework.extension.runtime.config.TagsConfigurationProperties;
import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import org.axonframework.axonserver.connector.AxonServerConfiguration;
import org.axonframework.axonserver.connector.AxonServerConnectionManager;
import org.axonframework.axonserver.connector.ManagedChannelCustomizer;
import org.axonframework.axonserver.connector.TargetContextResolver;
import org.axonframework.axonserver.connector.command.CommandLoadFactorProvider;
import org.axonframework.axonserver.connector.command.CommandPriorityCalculator;
import org.axonframework.axonserver.connector.event.axon.AxonServerEventScheduler;
import org.axonframework.axonserver.connector.query.QueryPriorityCalculator;
import org.axonframework.commandhandling.distributed.AnnotationRoutingStrategy;
import org.axonframework.commandhandling.distributed.RoutingStrategy;
import org.axonframework.eventhandling.scheduling.EventScheduler;
import org.axonframework.queryhandling.LoggingQueryInvocationErrorHandler;
import org.axonframework.queryhandling.QueryInvocationErrorHandler;
import org.axonframework.serialization.Serializer;

@ApplicationScoped
@IfBuildProperty(name = "axon.axonserver.enabled", stringValue = "true", enableIfMissing = true)
public class AxonServerConfigurationProducer {

//    private List<Object> sagas = new ArrayList<>();
//    private List<Object> aggregates = new ArrayList<>();
//    private List<Object> commandHandlers = new ArrayList<>();
//    private List<Object> eventHandlers = new ArrayList<>();
//    private List<Object> queryHandlers = new ArrayList<>();
//
    @Produces
    @Dependent
    public AxonServerConfiguration axonServerConfiguration(AxonRuntimeConfig axonRuntimeConfig) {
        AxonServerConfiguration.Builder builder = AxonServerConfiguration.builder();
        axonRuntimeConfig.clientId().ifPresent(builder::clientId);
        axonRuntimeConfig.componentName().ifPresent(builder::componentName);

        axonRuntimeConfig.servers().ifPresent(builder::servers);
        if (axonRuntimeConfig.sslEnabled()) {
            axonRuntimeConfig.certFile().ifPresent(builder::ssl);
        }

        axonRuntimeConfig.token().ifPresent(builder::token);
        axonRuntimeConfig.eventSecretKey().ifPresent(builder::setEventSecretKey);

        axonRuntimeConfig.context().ifPresent(builder::context);
        axonRuntimeConfig.maxMessageSize().ifPresent(builder::maxMessageSize);
        axonRuntimeConfig.snapshotPrefetch().ifPresent(builder::snapshotPrefetch);

        return builder.build();
    }

    private String clientName(@Nullable String id) {
        if (id == null) {
            return "Unnamed";
        } else if (id.contains(":")) {
            return id.substring(0, id.indexOf(":"));
        }
        return id;
    }

    @Produces
    @DefaultBean
    @ApplicationScoped
    public ManagedChannelCustomizer managedChannelCustomizer() {
        return ManagedChannelCustomizer.identity();
    }

    @Produces
    @Dependent
    public AxonServerConnectionManager platformConnectionManager(AxonServerConfiguration axonServerConfiguration,
                                                                 TagsConfigurationProperties tagsConfigurationProperties,
                                                                 ManagedChannelCustomizer managedChannelCustomizer) {
        return AxonServerConnectionManager.builder()
                .axonServerConfiguration(axonServerConfiguration)
//                .tagsConfiguration(tagsConfigurationProperties.toTagsConfiguration())
                .channelCustomizer(managedChannelCustomizer)
                .build();
    }

    @Produces
    @DefaultBean
    @ApplicationScoped
    public RoutingStrategy routingStrategy() {
        return AnnotationRoutingStrategy.defaultStrategy();
    }

    @Produces
    @DefaultBean
    @ApplicationScoped
    public CommandPriorityCalculator commandPriorityCalculator() {
        return CommandPriorityCalculator.defaultCommandPriorityCalculator();
    }

    @Produces
    @DefaultBean
    @ApplicationScoped
    public CommandLoadFactorProvider commandLoadFactorProvider(AxonServerConfiguration configuration) {
        return command -> configuration.getCommandLoadFactor();
    }

    @Produces
    @DefaultBean
    @ApplicationScoped
    public QueryPriorityCalculator queryPriorityCalculator() {
        return QueryPriorityCalculator.defaultQueryPriorityCalculator();
    }

    @Produces
    @DefaultBean
    @ApplicationScoped
    public QueryInvocationErrorHandler queryInvocationErrorHandler() {
        return LoggingQueryInvocationErrorHandler.builder().build();
    }

    @Produces
    @DefaultBean
    @Dependent
    public TargetContextResolver targetContextResolver() {
        return TargetContextResolver.noOp();
    }



//    @Bean
//    @ConditionalOnMissingClass("org.axonframework.extensions.multitenancy.autoconfig.MultiTenancyAxonServerAutoConfiguration")
//    public EventProcessorInfoConfiguration processorInfoConfiguration(
//            EventProcessingConfiguration eventProcessingConfiguration,
//            AxonServerConnectionManager connectionManager,
//            AxonServerConfiguration configuration) {
//        return new EventProcessorInfoConfiguration(c -> eventProcessingConfiguration,
//                c -> connectionManager,
//                c -> configuration);
//    }

    @Produces
    @DefaultBean
    @ApplicationScoped
    public EventScheduler eventScheduler(@AxonSerializer(type = AxonSerializer.SerializerType.EVENT) Serializer eventSerializer,
                                         AxonServerConnectionManager connectionManager) {
        return AxonServerEventScheduler.builder()
                .eventSerializer(eventSerializer)
                .connectionManager(connectionManager)
                .build();
    }

}
