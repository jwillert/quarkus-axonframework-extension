package de.jwillert.quarkus.axonframework.extension.runtime.cdi;

import com.thoughtworks.xstream.XStream;
import de.jwillert.quarkus.axonframework.extension.runtime.cdi.qualifier.AxonSerializer;
import de.jwillert.quarkus.axonframework.extension.runtime.cdi.qualifier.LocalSegment;
import de.jwillert.quarkus.axonframework.extension.runtime.config.SerializerProperties;
import de.jwillert.quarkus.axonframework.extension.runtime.config.TagsConfigurationProperties;
import io.quarkus.arc.DefaultBean;
import de.jwillert.quarkus.axonframework.extension.runtime.config.EventProcessorProperties;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.*;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import org.axonframework.axonserver.connector.AxonServerConfiguration;
import org.axonframework.axonserver.connector.AxonServerConnectionManager;
import org.axonframework.axonserver.connector.TargetContextResolver;
import org.axonframework.axonserver.connector.query.AxonServerQueryBus;
import org.axonframework.axonserver.connector.query.QueryPriorityCalculator;
import org.axonframework.commandhandling.*;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.common.transaction.NoTransactionManager;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.*;
import org.axonframework.eventhandling.*;
import org.axonframework.eventhandling.async.SequencingPolicy;
import org.axonframework.eventhandling.async.SequentialPerAggregatePolicy;
import org.axonframework.eventhandling.gateway.DefaultEventGateway;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.messaging.StreamableMessageSource;
import org.axonframework.messaging.correlation.CorrelationDataProvider;
import org.axonframework.messaging.correlation.MessageOriginProvider;
import org.axonframework.messaging.interceptors.CorrelationDataInterceptor;
import org.axonframework.queryhandling.*;
import org.axonframework.serialization.*;
import org.axonframework.serialization.xml.XStreamSerializer;
import org.axonframework.tracing.SpanFactory;
import org.jboss.logging.Logger;

import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class AxonConfigurationProducer {

    private static final Logger logger = Logger.getLogger(AxonConfigurationProducer.class);

    @Inject
    RegisteredAnnotatedTypes recordedClasses;



    //    private AxonRuntimeConfig axonRuntimeConfig;
//
//    private List<Object> sagas = new ArrayList<>();
//    private List<Object> aggregates = new ArrayList<>();
//    private List<Object> commandHandlers = new ArrayList<>();
//    private List<Object> eventHandlers = new ArrayList<>();
//    private List<Object> queryHandlers = new ArrayList<>();
//
//    public void setAxonRuntimeConfig(AxonRuntimeConfig axonRuntimeConfig) {
//        this.axonRuntimeConfig = axonRuntimeConfig;
//    }
//
    @Produces
    @ApplicationScoped
    public Configuration initializeConfiguration(Configurer configurer) {

        Configuration configuration = configurer.buildConfiguration();

        if (recordedClasses.getAxonRuntimeConfig().autostart()) {
            configuration.start();
        }

        return configuration;
    }

    @Produces
    @ApplicationScoped
    public Configurer quarkusDefaultConfigurer(SpanFactory spanFactory,
                                               EventBus eventBus,
                                               CommandBus commandBus,
                                               QueryBus queryBus,
                                               Serializer serializer) {


        Configurer configurer = DefaultConfigurer.defaultConfiguration(false)
                .configureEventBus(c -> eventBus)
                .configureCommandBus(c -> commandBus)
                .configureQueryBus(c -> queryBus)
                .configureSerializer(c -> serializer);


        logger.info(String.format("Register flowing Aggregates [%s]", recordedClasses.getAggregates().stream().map(Object::getClass).map(Class::getSimpleName).collect(Collectors.joining(", "))));

        // When registering a Saga or Aggregate also the EventSourcingHandlers inside
        // of that class are registered
        recordedClasses.getSagas().forEach(saga -> configurer.eventProcessing().registerSaga(saga.getClass()));
        recordedClasses.getAggregates().forEach(aggregate -> configurer.configureAggregate(aggregate.getClass()));

        // Command handlers, Event handlers and Query handlers must be registered by itself.
        recordedClasses.getCommandHandlers().forEach(commandHandler -> configurer.registerCommandHandler(conf -> commandHandler));
        recordedClasses.getEventHandlers().forEach(eventHandler -> configurer.registerEventHandler(conf -> eventHandler));
        recordedClasses.getQueryHandlers().forEach(queryHandler -> configurer.registerQueryHandler(conf -> queryHandler));

        configurer.configureSpanFactory(configuration -> spanFactory);

        return configurer;
    }

    @Produces
    @ApplicationScoped
    public EventProcessingConfigurer eventProcessingConfigurer() {
        return new EventProcessingModule();
    }

    @Produces
    @ApplicationScoped
    public TagsConfiguration tagsConfiguration(TagsConfigurationProperties tagsConfigurationProperties) {
        return tagsConfigurationProperties.toTagsConfiguration();
    }

    @Produces
    @ApplicationScoped
    public RevisionResolver revisionResolver() {
        return new AnnotationRevisionResolver();
    }

    @Produces
    @ApplicationScoped
    @Default
    @AxonSerializer(type = AxonSerializer.SerializerType.DEFAULT)
    public Serializer serializer(SerializerProperties serializerProperties, RevisionResolver revisionResolver) {
        return buildSerializer(revisionResolver, serializerProperties.general());
    }

    @Produces
    @Specializes
    @AxonSerializer(type = AxonSerializer.SerializerType.MESSAGE)
    @ApplicationScoped
    public Serializer messageSerializer(SerializerProperties serializerProperties, Serializer genericSerializer, RevisionResolver revisionResolver) {
        if (SerializerProperties.SerializerType.DEFAULT.equals(serializerProperties.messages())
                || serializerProperties.general().equals(serializerProperties.messages())) {
            return genericSerializer;
        }
        return buildSerializer(revisionResolver, serializerProperties.messages());
    }

    @Produces
    @AxonSerializer(type = AxonSerializer.SerializerType.EVENT)
    @ApplicationScoped
    public Serializer eventSerializer(SerializerProperties serializerProperties,
                                      @AxonSerializer(type = AxonSerializer.SerializerType.MESSAGE) Serializer messageSerializer,
                                      Serializer generalSerializer,
                                      RevisionResolver revisionResolver) {
        if (SerializerProperties.SerializerType.DEFAULT.equals(serializerProperties.events())
                || serializerProperties.events().equals(serializerProperties.messages())) {
            return messageSerializer;
        } else if (serializerProperties.general().equals(serializerProperties.events())) {
            return generalSerializer;
        }
        return buildSerializer(revisionResolver, serializerProperties.events());
    }

    @Produces
    @ApplicationScoped
    public ConfigurerModule serializerConfigurer(@AxonSerializer(type = AxonSerializer.SerializerType.EVENT)  Serializer eventSerializer,
                                                 @AxonSerializer(type = AxonSerializer.SerializerType.MESSAGE) Serializer messageSerializer,
                                                 @AxonSerializer(type = AxonSerializer.SerializerType.DEFAULT) Serializer generalSerializer) {
        return configurer -> {
            configurer.configureEventSerializer(c -> eventSerializer);
            configurer.configureMessageSerializer(c -> messageSerializer);
            configurer.configureSerializer(c -> generalSerializer);
        };
    }

    private Serializer buildSerializer(RevisionResolver revisionResolver,
                                       SerializerProperties.SerializerType serializerType) {

        switch (serializerType) {
            case JACKSON:
//                Map<String, ObjectMapper> objectMapperBeans = beansOfTypeIncludingAncestors(applicationContext, ObjectMapper.class);
//                ObjectMapper objectMapper = objectMapperBeans.containsKey("defaultAxonObjectMapper")
//                        ? objectMapperBeans.get("defaultAxonObjectMapper")
//                        : objectMapperBeans.values().stream().findFirst()
//                        .orElseThrow(() -> new NoSuchBeanDefinitionException(ObjectMapper.class));
//                ChainingConverter converter = new ChainingConverter(beanClassLoader);
//                return JacksonSerializer.builder()
//                        .revisionResolver(revisionResolver)
//                        .converter(converter)
//                        .objectMapper(objectMapper)
//                        .build();
            case JAVA:
//                return JavaSerializer.builder().revisionResolver(revisionResolver).build();
            case XSTREAM:
            case DEFAULT:
            default:
//                Map<String, XStream> xStreamBeans = beansOfTypeIncludingAncestors(applicationContext, XStream.class);


//                Instance<XStream> defaultAxonXStream = CDI.current().select(XStream.class, NamedLiteral.of("defaultAxonXStream"));
//
                Instance<XStream> xStream = null;
//                if (defaultAxonXStream.isResolvable()) {
//                    xStream = defaultAxonXStream;
//                } else {
                    xStream = CDI.current().select(XStream.class, new Default.Literal());
//                }

                return XStreamSerializer.builder()
                        .xStream(xStream.get())
//                        .revisionResolver(revisionResolver)
//                        .classLoader(beanClassLoader)
                        .build();
        }
    }

    @Produces
    @ApplicationScoped
    public CorrelationDataProvider messageOriginProvider() {
        return new MessageOriginProvider();
    }

//    @Produces
//    @ApplicationScoped
//    @DefaultBean
//    public EventStore eventStore(EventStorageEngine storageEngine, Configuration configuration) {
//        return EmbeddedEventStore.builder()
//                .storageEngine(storageEngine)
//                .messageMonitor(configuration.messageMonitor(EventStore.class, "eventStore"))
//                .spanFactory(configuration.spanFactory())
//                .build();
//    }

    @Produces
    @Dependent
    @DefaultBean
    @Default
    public CommandGateway commandGateway(CommandBus commandBus) {
//        return configuration.commandGateway();
        return DefaultCommandGateway.builder()
                .commandBus(commandBus).build();


    }

    @Produces
    @ApplicationScoped
    @DefaultBean
    @Default
    public QueryGateway queryGateway(@Default QueryBus queryBus) {
        return DefaultQueryGateway.builder().queryBus(queryBus).build();
    }

    @Produces
    @DefaultBean
    @ApplicationScoped
    public TransactionManager transactionManager() {
        return NoTransactionManager.instance();
    }

//    @Produces
//    @ApplicationScoped
//    public SimpleEventBus eventBus(Configuration configuration) {
//        return SimpleEventBus.builder()
//                .messageMonitor(configuration.messageMonitor(EventStore.class, "eventStore"))
//                .spanFactory(configuration.spanFactory())
//                .build();
//    }

    @Produces
    @ApplicationScoped
    public EventGateway eventGateway(EventBus eventBus) {
        return DefaultEventGateway.builder().eventBus(eventBus).build();
    }

//    @ConditionalOnMissingBean(Snapshotter.class)
//    @ConditionalOnBean(EventStore.class)
//    @Bean
//    public SpringAggregateSnapshotter aggregateSnapshotter(Configuration configuration,
//                                                           HandlerDefinition handlerDefinition,
//                                                           ParameterResolverFactory parameterResolverFactory,
//                                                           EventStore eventStore,
//                                                           TransactionManager transactionManager,
//                                                           SpanFactory spanFactory) {
//        return SpringAggregateSnapshotter.builder()
//                .repositoryProvider(configuration::repository)
//                .transactionManager(transactionManager)
//                .eventStore(eventStore)
//                .parameterResolverFactory(parameterResolverFactory)
//                .handlerDefinition(handlerDefinition)
//                .spanFactory(spanFactory)
//                .build();
//    }

    @SuppressWarnings("unchecked")
    public void configureEventHandling(@Observes EventProcessingConfigurer eventProcessingConfigurer,
                                       EventProcessorProperties eventProcessorProperties,
                                       BeanManager beanManager) {
//        eventProcessorProperties.getProcessors().forEach((name, settings) -> {
//            Function<Configuration, SequencingPolicy<? super EventMessage<?>>> sequencingPolicy =
//                    resolveSequencingPolicy(beanManager, settings);
//            eventProcessingConfigurer.registerSequencingPolicy(name, sequencingPolicy);
//
//            if (settings.mode() == EventProcessorProperties.Mode.TRACKING) {
//                TrackingEventProcessorConfiguration config = TrackingEventProcessorConfiguration
//                        .forParallelProcessing(settings.threadCount())
//                        .andBatchSize(settings.batchSize())
//                        .andInitialSegmentsCount(initialSegmentCount(settings, 1))
//                        .andTokenClaimInterval(settings.tokenClaimInterval(),
//                                settings.tokenClaimIntervalTimeUnit());
//                Function<Configuration, StreamableMessageSource<TrackedEventMessage<?>>> messageSource =
//                        resolveMessageSource(beanManager, settings);
//                eventProcessingConfigurer.registerTrackingEventProcessor(name, messageSource, c -> config);
//            } else if (settings.mode() == EventProcessorProperties.Mode.POOLED) {
//                eventProcessingConfigurer.registerPooledStreamingEventProcessor(
//                        name,
//                        resolveMessageSource(beanManager, settings),
//                        (config, builder) -> {
//                            ScheduledExecutorService workerExecutor = Executors.newScheduledThreadPool(
//                                    settings.threadCount(), new AxonThreadFactory("WorkPackage[" + name + "]")
//                            );
//                            config.onShutdown(workerExecutor::shutdown);
//                            return builder.workerExecutor(workerExecutor)
//                                    .initialSegmentCount(initialSegmentCount(settings, 16))
//                                    .tokenClaimInterval(tokenClaimIntervalMillis(settings))
//                                    .batchSize(settings.batchSize());
//                        }
//                );
//            } else {
////                if (settings.getSource() == null) {
//                eventProcessingConfigurer.registerSubscribingEventProcessor(name);
////                } else {
////                    eventProcessingConfigurer.registerSubscribingEventProcessor(
////                            name,
////                            c -> CDI.current().select(Class.forName(settings.getSource()));
////                    );
////                }
//            }
//        });
    }

    private int initialSegmentCount(EventProcessorProperties.ProcessorSettings settings, int defaultCount) {
        return settings.initialSegmentCount() != null ? settings.initialSegmentCount() : defaultCount;
    }

    private long tokenClaimIntervalMillis(EventProcessorProperties.ProcessorSettings settings) {
        return settings.tokenClaimIntervalTimeUnit().toMillis(settings.tokenClaimInterval());
    }

    @SuppressWarnings("unchecked")
    private Function<Configuration, StreamableMessageSource<TrackedEventMessage<?>>> resolveMessageSource(
            BeanManager beanManager, EventProcessorProperties.ProcessorSettings v) {
        Function<Configuration, StreamableMessageSource<TrackedEventMessage<?>>> messageSource;
        if (v.source() == null) {
            messageSource = Configuration::eventStore;
        } else {
//            )beanManager.r.getBean(v.getSource(),
            messageSource = c -> CDI.current().select(StreamableMessageSource.class).get();
        }
        return messageSource;
    }

    @SuppressWarnings("unchecked")
    private Function<Configuration, SequencingPolicy<? super EventMessage<?>>> resolveSequencingPolicy(
            BeanManager beanManager, EventProcessorProperties.ProcessorSettings v) {
        Function<Configuration, SequencingPolicy<? super EventMessage<?>>> sequencingPolicy;
        if (v.sequencingPolicy() != null) {
            sequencingPolicy = c -> CDI.current().select(SequencingPolicy.class).get();
//            sequencingPolicy = c -> applicationContext.getBean(v.getSequencingPolicy(), SequencingPolicy.class);
        } else {
            sequencingPolicy = c -> SequentialPerAggregatePolicy.instance();
        }
        return sequencingPolicy;
    }

    @Produces
    @ApplicationScoped
    public DuplicateCommandHandlerResolver duplicateCommandHandlerResolver() {
        return LoggingDuplicateCommandHandlerResolver.instance();
    }

    @Produces
    @LocalSegment
    @ApplicationScoped
    public CommandBus commandBus(TransactionManager txManager,
                                 Configuration axonConfiguration,
                                 DuplicateCommandHandlerResolver duplicateCommandHandlerResolver) {
        SimpleCommandBus commandBus =
                SimpleCommandBus.builder()
                        .transactionManager(txManager)
                        .duplicateCommandHandlerResolver(duplicateCommandHandlerResolver)
                        .build();
//        commandBus.registerHandlerInterceptor(
//                new CorrelationDataInterceptor<>(axonConfiguration.correlationDataProviders())
//        );
        return commandBus;
    }

    @Produces
    @DefaultBean
    @Default
    @ApplicationScoped
    public QueryBus queryBus(AxonServerConnectionManager axonServerConnectionManager,
                             AxonServerConfiguration axonServerConfiguration,
                             org.axonframework.config.Configuration axonConfiguration,
                             TransactionManager txManager,
                             @AxonSerializer(type = AxonSerializer.SerializerType.MESSAGE) Serializer messageSerializer,
                             Serializer genericSerializer,
                             QueryPriorityCalculator priorityCalculator,
                             QueryInvocationErrorHandler queryInvocationErrorHandler,
                             TargetContextResolver targetContextResolver) {
        SimpleQueryBus simpleQueryBus =
                SimpleQueryBus.builder()
                        .messageMonitor(axonConfiguration.messageMonitor(QueryBus.class, "queryBus"))
                        .transactionManager(txManager)
                        .queryUpdateEmitter(axonConfiguration.getComponent(QueryUpdateEmitter.class))
                        .errorHandler(queryInvocationErrorHandler)
                        .spanFactory(axonConfiguration.spanFactory())
                        .build();
        simpleQueryBus.registerHandlerInterceptor(
                new CorrelationDataInterceptor<>(axonConfiguration.correlationDataProviders())
        );

        return AxonServerQueryBus.builder()
                .axonServerConnectionManager(axonServerConnectionManager)
                .configuration(axonServerConfiguration)
                .localSegment(simpleQueryBus)
                .updateEmitter(simpleQueryBus.queryUpdateEmitter())
                .messageSerializer(messageSerializer)
                .genericSerializer(genericSerializer)
                .priorityCalculator(priorityCalculator)
                .targetContextResolver(targetContextResolver)
                .spanFactory(axonConfiguration.spanFactory())
                .build();
    }

    @LocalSegment
    @Produces
    @ApplicationScoped
    public QueryBus queryBus(Configuration axonConfiguration, TransactionManager transactionManager) {
        return SimpleQueryBus.builder()
                .messageMonitor(axonConfiguration.messageMonitor(QueryBus.class, "queryBus"))
                .transactionManager(transactionManager)
                .errorHandler(axonConfiguration.getComponent(
                        QueryInvocationErrorHandler.class,
                        () -> LoggingQueryInvocationErrorHandler.builder().build()
                ))
                .queryUpdateEmitter(axonConfiguration.getComponent(QueryUpdateEmitter.class))
                .spanFactory(axonConfiguration.spanFactory())
                .build();
    }

    @Produces
    @ApplicationScoped
    public QueryUpdateEmitter queryUpdateEmitter(Configuration configuration) {
        return SimpleQueryUpdateEmitter.builder()
                .updateMessageMonitor(configuration.messageMonitor(
                        QueryUpdateEmitter.class, "queryUpdateEmitter"
                ))
                .spanFactory(configuration.spanFactory())
                .build();
    }

}
