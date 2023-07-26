package de.jwillert.quarkus.axonframework.extension.runtime.cdi;

import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.axonframework.tracing.*;

@ApplicationScoped
public class AxonTracingAutoConfiguration {

    @Produces
    @DefaultBean
    public SpanFactory spanFactory() {
        return LoggingSpanFactory.INSTANCE;
    }

}
