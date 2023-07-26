package de.jwillert.quarkus.axonframework.extension.runtime;

import de.jwillert.quarkus.axonframework.extension.runtime.cdi.RegisteredAnnotatedTypes;
import org.axonframework.config.Configuration;
import org.jboss.logging.Logger;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.annotations.Recorder;

import java.util.List;

@Recorder
public class AxonRuntimeRecorder {

    private static final Logger log = Logger.getLogger(AxonRuntimeRecorder.class);

    public void setAxonBuildConfig(BeanContainer container, AxonRuntimeConfig axonRuntimeConfig) {
        container.instance(RegisteredAnnotatedTypes.class).setAxonRuntimeConfig(axonRuntimeConfig);
    }

    public void registerSaga(BeanContainer container, Class<?> axonAnnotatedClass) {
        container.instance(RegisteredAnnotatedTypes.class).registerSaga(container.instance(axonAnnotatedClass));
    }

    public void registerBasePackage(BeanContainer container, List<String> basePackage) {
        container.instance(RegisteredAnnotatedTypes.class).setBasepackage(basePackage);
    }

    public void registerAggregate(BeanContainer container, Class<?> axonAnnotatedClass) {
        container.instance(RegisteredAnnotatedTypes.class).registerAggregate(container.instance(axonAnnotatedClass));
    }

    public void registerEventHandler(BeanContainer container, Class<?> axonAnnotatedClass) {
        container.instance(RegisteredAnnotatedTypes.class).registerEventHandler(container.instance(axonAnnotatedClass));
    }

    public void registerCommandHandler(BeanContainer container, Class<?> axonAnnotatedClass) {
        container.instance(RegisteredAnnotatedTypes.class).registerCommandHandler(container.instance(axonAnnotatedClass));
    }

    public void registerQueryHandler(BeanContainer container, Class<?> axonAnnotatedClass) {
        container.instance(RegisteredAnnotatedTypes.class).registerQueryHandler(container.instance(axonAnnotatedClass));
    }

    public Configuration initializeAxonClient(BeanContainer container) {
        Configuration configuration = container.instance(Configuration.class);

        configuration.onStart(() -> {
            log.info("Axon framework started");
        });

        configuration.onShutdown(() -> {
            log.info("Axon framework stopped");
        });

        return configuration;
    }

    public void injectBeanContainerIntoBeanResolverFactory(BeanContainer container) {
        AxonArcBeanResolverFactory.setBeanContainer(container);
    }
}
