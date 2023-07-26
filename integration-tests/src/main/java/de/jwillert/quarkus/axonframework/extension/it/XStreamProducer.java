package de.jwillert.quarkus.axonframework.extension.it;

import com.thoughtworks.xstream.XStream;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class XStreamProducer {


    @Unremovable
    @Produces
    @Default
    public XStream xStream() {
        XStream xStream = new XStream();

        xStream.allowTypesByWildcard(new String[] {
                "de.jwillert.quarkus.axonframework.extension.it.**"
        });
        return xStream;
    }

}
