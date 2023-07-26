package de.jwillert.quarkus.axonframework.extension.runtime.stereotype;

import jakarta.enterprise.inject.Stereotype;
import jakarta.inject.Named;
import org.axonframework.modelling.command.AggregateRoot;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Stereotype
@AggregateRoot
public @interface Aggregate {

}
