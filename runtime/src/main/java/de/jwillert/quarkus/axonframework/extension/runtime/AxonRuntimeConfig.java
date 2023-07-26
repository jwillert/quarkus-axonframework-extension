package de.jwillert.quarkus.axonframework.extension.runtime;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * The Axon configuration.
 */
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "axon")
public interface AxonRuntimeConfig {

    /**
     * Autostart the Axon client on start of the application.
     *
     * Default: true
     */
    @WithDefault("true")
    boolean autostart();

    /**
     * The id of the Axon client
     */
    Optional<String> clientId();

    /**
     * The name of the component
     */
    
    Optional<String> componentName();

    /**
     * The Axon server locations or locations
     * For example: 'localhost:1234, 10.12.14.1:4321'
     *
     * Default: localhost (will also use default port 8124)
     */
    Optional<String> servers();

    /**
     * Enable SSL. Requires also a certFile
     */
    @WithDefault("false")
    @WithName("ssl-enabled")
    boolean sslEnabled();

    /**
     * The location of the SSL certFile
     */
    Optional<String> certFile();

    /**
     * token
     */
    Optional<String> token();

    /**
     * eventSecretKey
     */
    Optional<String> eventSecretKey();

    /**
     * context
     */
    Optional<String> context();

    /**
     * maxMessageSize
     */
    Optional<Integer> maxMessageSize();

    /**
     * snapshotPrefetch
     */
    Optional<Integer> snapshotPrefetch();

}
