package de.jwillert.quarkus.axonframework.extension.runtime.config;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "axon.eventhandling")
public interface EventProcessorProperties {

    /**
     * Returns the settings for each of the configured processors, by name.
     *
     * @return the settings for each of the configured processors, by name.
     */
    Map<String, ProcessorSettings> getProcessors();

    /**
     * The processing modes of an {@link org.axonframework.eventhandling.EventProcessor}.
     */
    public enum Mode {
        /**
         * Indicates a {@link org.axonframework.eventhandling.TrackingEventProcessor} should be used.
         */
        TRACKING,
        /**
         * Indicates a {@link org.axonframework.eventhandling.SubscribingEventProcessor} should be used.
         */
        SUBSCRIBING,
        /**
         * Indicates a {@link org.axonframework.eventhandling.pooled.PooledStreamingEventProcessor} should be used.
         */
        POOLED
    }

    interface ProcessorSettings {

        /**
         * Sets the source for this processor.
         * <p>
         * Defaults to streaming from the {@link org.axonframework.eventsourcing.eventstore.EventStore} when the {@link
         * #mode} is set to {@link Mode#TRACKING} or {@link Mode#POOLED}, and to subscribing to the {@link
         * org.axonframework.eventhandling.EventBus} when the {@link #mode} is set to {@link Mode#SUBSCRIBING}.
         */
        String source();

        /**
         * Indicates whether this processor should be Tracking, or Subscribing its source. Defaults to {@link
         * Mode#TRACKING}.
         */
        @WithDefault("TRACKING")
        Mode mode();

        /**
         * Indicates the number of segments that should be created when the processor starts for the first time.
         * Defaults to 1 for a {@link org.axonframework.eventhandling.TrackingEventProcessor} and 16 for a {@link
         * org.axonframework.eventhandling.pooled.PooledStreamingEventProcessor}.
         */
        Integer initialSegmentCount();

        /**
         * The interval between attempts to claim tokens by a {@link org.axonframework.eventhandling.StreamingEventProcessor}.
         * <p>
         * Defaults to 5000 milliseconds.
         */
        @WithDefault("5000")
        long tokenClaimInterval();

        /**
         * The time unit of tokens claim interval.
         * <p>
         * Defaults to {@link TimeUnit#MILLISECONDS}.
         */
        @WithDefault("MILLISECONDS")
        TimeUnit tokenClaimIntervalTimeUnit();

        /**
         * The maximum number of threads the processor should process events with. Defaults to the number of initial
         * segments if this is not further specified. Defaults to 1 for a {@link org.axonframework.eventhandling.TrackingEventProcessor}
         * and 4 for a {@link org.axonframework.eventhandling.pooled.PooledStreamingEventProcessor}.
         */
        @WithDefault("1")
        int threadCount();

        /**
         * The maximum number of events a processor should process as part of a single batch.
         */
        @WithDefault("1")
        int batchSize();

        /**
         * The name of the bean that represents the sequencing policy for processing events. If no name is specified,
         * the processor defaults to a {@link org.axonframework.eventhandling.async.SequentialPerAggregatePolicy}, which
         * guarantees to process events originating from the same Aggregate instance sequentially, while events from
         * different Aggregate instances may be processed concurrently.
         */
        String sequencingPolicy();

        /**
         * The {@link org.axonframework.messaging.deadletter.SequencedDeadLetterQueue} settings that will be used for this processing group.
         */
        Dlq dlq();

    }

    interface Dlq {

        /**
         * Enables creation and configuring a {@link org.axonframework.messaging.deadletter.SequencedDeadLetterQueue}.
         * Will be used to configure the {@code registerDeadLetterQueueProvider} such that only groups set to enabled
         * will have a sequenced dead letter queue. Defaults to "false".
         */
        @WithDefault("false")
        boolean enabled();

    }
}
