package de.jwillert.quarkus.axonframework.extension.deployment;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import de.jwillert.quarkus.axonframework.extension.runtime.AxonRuntimeRecorder;
import de.jwillert.quarkus.axonframework.extension.runtime.cdi.*;
import de.jwillert.quarkus.axonframework.extension.runtime.stereotype.Aggregate;
import de.jwillert.quarkus.axonframework.extension.runtime.stereotype.Saga;
import io.quarkus.deployment.builditem.ApplicationArchivesBuildItem;
import io.quarkus.deployment.builditem.ApplicationIndexBuildItem;
import io.quarkus.deployment.builditem.QuarkusApplicationClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedPackageBuildItem;
import io.quarkus.deployment.pkg.builditem.PackageTypeBuildItem;
import io.quarkus.runtime.Quarkus;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.jboss.jandex.*;
import org.jboss.logging.Logger;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanArchiveIndexBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import de.jwillert.quarkus.axonframework.extension.runtime.AxonRuntimeConfig;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class AxonProcessor {

    private static final Logger LOGGER = Logger.getLogger(AxonProcessor.class);

    private static DotName AGGREGATE_ANNOTATION = DotName.createSimple(Aggregate.class.getName());
    private static DotName SAGA_ANNOTATION = DotName.createSimple(Saga.class.getName());
    private static DotName COMMAND_HANDLER_ANNOTATION = DotName.createSimple(CommandHandler.class.getName());
    private static DotName EVENT_HANDLER_ANNOTATION = DotName.createSimple(EventHandler.class.getName());
    private static DotName QUERY_HANDLER_ANNOTATION = DotName.createSimple(QueryHandler.class.getName());

    @BuildStep
    public void build(BuildProducer<AdditionalBeanBuildItem> additionalBeanProducer, BuildProducer<FeatureBuildItem> featureProducer) {

        // featureProducer.produce(new FeatureBuildItem(FeatureBuildItem.AXON-FRAMEWORK));
        featureProducer.produce(new FeatureBuildItem("axon-framework"));

        additionalBeanProducer.produce(AdditionalBeanBuildItem.unremovableOf(RegisteredAnnotatedTypes.class));
        additionalBeanProducer.produce(AdditionalBeanBuildItem.unremovableOf(AxonConfigurationProducer.class));
        additionalBeanProducer.produce(AdditionalBeanBuildItem.unremovableOf(AxonServerBusConfigurationProducer.class));
        additionalBeanProducer.produce(AdditionalBeanBuildItem.unremovableOf(AxonServerConfigurationProducer.class));
        additionalBeanProducer.produce(AdditionalBeanBuildItem.unremovableOf(AxonTracingAutoConfiguration.class));
        additionalBeanProducer.produce(AdditionalBeanBuildItem.unremovableOf(XStreamAutoProducer.class));
//                additionalBeanProducer.produce(AdditionalBeanBuildItem.unremovableOf(RegisteredAnnotatedTypes.class));
    }

    @BuildStep()
    void scanForAggregates(BeanArchiveIndexBuildItem beanArchiveIndex, BuildProducer<AggregateBuildItem> axonBuildItemProducer) {
        scanForBeans(beanArchiveIndex, axonBuildItemProducer, AGGREGATE_ANNOTATION, AggregateBuildItem::new);
    }

    @BuildStep
    void scanForSagas(BeanArchiveIndexBuildItem beanArchiveIndex, BuildProducer<SagaBuildItem> axonBuildItemProducer) {
        scanForBeans(beanArchiveIndex, axonBuildItemProducer, SAGA_ANNOTATION, SagaBuildItem::new);
    }

    @BuildStep
    void scanForCommandHandlers(BeanArchiveIndexBuildItem beanArchiveIndex, BuildProducer<CommandHandlerBuildItem> axonBuildItemProducer) {
        scanForBeans(beanArchiveIndex, axonBuildItemProducer, COMMAND_HANDLER_ANNOTATION, CommandHandlerBuildItem::new, AGGREGATE_ANNOTATION, SAGA_ANNOTATION);
    }

    @BuildStep
    void scanForEventHandlers(BeanArchiveIndexBuildItem beanArchiveIndex, BuildProducer<EventHandlerBuildItem> axonBuildItemProducer) {
        scanForBeans(beanArchiveIndex, axonBuildItemProducer, EVENT_HANDLER_ANNOTATION, EventHandlerBuildItem::new, AGGREGATE_ANNOTATION, SAGA_ANNOTATION);
    }

    @BuildStep
    void scanForQueryHandlers(BeanArchiveIndexBuildItem beanArchiveIndex, BuildProducer<QueryHandlerBuildItem> axonBuildItemProducer) {
        scanForBeans(beanArchiveIndex, axonBuildItemProducer, QUERY_HANDLER_ANNOTATION, QueryHandlerBuildItem::new, AGGREGATE_ANNOTATION, SAGA_ANNOTATION);
    }

    @BuildStep
    @Record(RUNTIME_INIT)
    void injectBeanContainerIntoBeanResolverFactory(AxonRuntimeRecorder template, BeanContainerBuildItem beanContainer) {
        template.injectBeanContainerIntoBeanResolverFactory(beanContainer.getValue());
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void configureRuntimeProperties(AxonRuntimeRecorder template, AxonRuntimeConfig axonRuntimeConfig, BeanContainerBuildItem beanContainer) {
        template.setAxonBuildConfig(beanContainer.getValue(), axonRuntimeConfig);
    }

    public static String getLongestCommonPrefix(String[] strings) {
        int commonPrefixLength = 0;
        while (allCharactersAreSame(strings, commonPrefixLength)) {
            commonPrefixLength++;
        }
        return strings[0].substring(0, commonPrefixLength);
    }

    private static boolean allCharactersAreSame(String[] strings, int pos) {
        String first = strings[0];
        for (String curString : strings) {
            if (curString.length() <= pos
                    || curString.charAt(pos) != first.charAt(pos)) {
                return false;
            }
        }
        return true;
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void registerSagas(AxonRuntimeRecorder template, List<SagaBuildItem> axonAnnotatedBeans, BeanContainerBuildItem beanContainer) {
        axonAnnotatedBeans.forEach(item -> template.registerSaga(beanContainer.getValue(), item.getAxonAnnotatedClass()));

    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void registerAggregates(AxonRuntimeRecorder template, List<AggregateBuildItem> axonAnnotatedBeans, BeanContainerBuildItem beanContainer) {
        axonAnnotatedBeans.forEach(item -> template.registerAggregate(beanContainer.getValue(), item.getAxonAnnotatedClass()));
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void registerCommandHandlers(AxonRuntimeRecorder template, List<CommandHandlerBuildItem> axonAnnotatedBeans, BeanContainerBuildItem beanContainer) {
        axonAnnotatedBeans.forEach(item -> template.registerCommandHandler(beanContainer.getValue(), item.getAxonAnnotatedClass()));
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void registerEventHandlers(AxonRuntimeRecorder template, List<EventHandlerBuildItem> axonAnnotatedBeans, BeanContainerBuildItem beanContainer) {
        axonAnnotatedBeans.forEach(item -> template.registerEventHandler(beanContainer.getValue(), item.getAxonAnnotatedClass()));
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void registerQueryHandlers(AxonRuntimeRecorder template, List<QueryHandlerBuildItem> axonAnnotatedBeans, BeanContainerBuildItem beanContainer) {
        axonAnnotatedBeans.forEach(item -> template.registerQueryHandler(beanContainer.getValue(), item.getAxonAnnotatedClass()));
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void initializeAxon(AxonRuntimeRecorder template, BeanContainerBuildItem beanContainer) {
        template.initializeAxonClient(beanContainer.getValue());
    }

    /**
     * This method scans for beans with the annotation type on class, field or method level and returns the classname
     */
    private <T extends AxonBuildItem> void scanForBeans(BeanArchiveIndexBuildItem beanArchiveIndex,
            BuildProducer<T> axonBuildItemProducer,
            DotName annotationType,
            Function<Class<?>, T> buildItem, DotName... ignoredAnnotations) {


        IndexView indexView = beanArchiveIndex.getIndex();
        Collection<AnnotationInstance> testBeans = indexView.getAnnotations(annotationType);

//        indexView.getKnownModules()

//        LOGGER.info(String.format("Scan for Beans with Annotation '%s' found: [%s]", annotationType.withoutPackagePrefix(), testBeans.));
        for (AnnotationInstance ann : testBeans) {

            try {
                ClassInfo beanClassInfo;
                if (AnnotationTarget.Kind.CLASS.equals(ann.target().kind())) {
                    beanClassInfo = ann.target().asClass();
                } else {
                    beanClassInfo = ann.target().asMethod().declaringClass();
                }

                if(Arrays.stream(ignoredAnnotations).anyMatch(ignoredAnnotation -> beanClassInfo.annotation(ignoredAnnotation) != null)) {
                    continue;
                }

                Class<?> beanClass = JandexReflection.loadClass(beanClassInfo);

                axonBuildItemProducer.produce(buildItem.apply(beanClass));

                LOGGER.info("Found " + annotationType.toString() + " annotation on class " + beanClass
                        + ". Item will be registered to Axon.");
            } catch (Exception e) {
                LOGGER.warn("Failed to load bean class", e);
            }
        }
    }

}
