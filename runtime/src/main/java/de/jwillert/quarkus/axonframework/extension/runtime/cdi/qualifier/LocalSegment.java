package de.jwillert.quarkus.axonframework.extension.runtime.cdi.qualifier;

import jakarta.inject.Qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.TYPE;

@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD,METHOD,PARAMETER,TYPE})
@Qualifier
public @interface LocalSegment {
}
