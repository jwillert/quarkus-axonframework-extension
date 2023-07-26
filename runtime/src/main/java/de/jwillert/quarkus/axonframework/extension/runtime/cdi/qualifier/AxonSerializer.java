package de.jwillert.quarkus.axonframework.extension.runtime.cdi.qualifier;

import jakarta.inject.Qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD,METHOD,PARAMETER,TYPE})
@Qualifier
public @interface AxonSerializer {

    SerializerType type();

    public enum SerializerType {
        DEFAULT, MESSAGE, EVENT
    }

}
