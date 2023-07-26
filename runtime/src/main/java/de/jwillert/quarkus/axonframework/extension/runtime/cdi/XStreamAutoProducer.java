package de.jwillert.quarkus.axonframework.extension.runtime.cdi;

import com.thoughtworks.xstream.XStream;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import org.axonframework.serialization.xml.CompactDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

@ApplicationScoped
public class XStreamAutoProducer {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

//    @DefaultBean
//    @Named("defaultAxonXStream")
//    @Produces
//    public XStream defaultAxonXStream(RegisteredAnnotatedTypes registeredAnnotatedTypes) {
//        logger.info(
//                "Initializing an XStream instance since none was found. "
//                        + "The auto configuration base packages will be used as wildcards for the XStream security settings."
//        );
//        XStream xStream = new XStream(new CompactDriver());
//        xStream.allowTypesByWildcard(new String[]{
//                "org.axonframework.**",
//        });
//
//        return xStream;
//    }

    /**
     * An {@link AnyNestedCondition} implementation, to support the following use cases:
     * <ul>
     *     <li>The {@code general} serializer property is not set. This means Axon defaults to XStream</li>
     *     <li>The {@code general} serializer property is set to {@code default}. This means XStream will be used</li>
     *     <li>The {@code general} serializer property is set to {@code xstream}</li>
     *     <li>The {@code messages} serializer property is set to {@code xstream}</li>
     *     <li>The {@code events} serializer property is set to {@code xstream}</li>
     * </ul>
     */
//    private static class XStreamConfiguredCondition {
//
//        public XStreamConfiguredCondition() {
//            super(ConfigurationPhase.REGISTER_BEAN);
//        }
//
//        @SuppressWarnings("unused")
//        @ConditionalOnProperty(name = "axon.serializer.general", havingValue = "default", matchIfMissing = true)
//        static class GeneralDefaultCondition {
//
//        }
//
//        @SuppressWarnings("unused")
//        @ConditionalOnProperty(name = "axon.serializer.general", havingValue = "xstream", matchIfMissing = true)
//        static class GeneralXStreamCondition {
//
//        }
//
//        @SuppressWarnings("unused")
//        @ConditionalOnProperty(name = "axon.serializer.messages", havingValue = "xstream")
//        static class MessagesXStreamCondition {
//
//        }
//
//        @SuppressWarnings("unused")
//        @ConditionalOnProperty(name = "axon.serializer.events", havingValue = "xstream")
//        static class EventsXStreamCondition {
//
//        }
//    }


}
