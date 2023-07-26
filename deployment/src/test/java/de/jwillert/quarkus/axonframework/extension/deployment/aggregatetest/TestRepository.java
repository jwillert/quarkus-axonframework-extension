package de.jwillert.quarkus.axonframework.extension.deployment.aggregatetest;

import jakarta.enterprise.context.Dependent;

import org.jboss.logging.Logger;

@Dependent
public class TestRepository {
    private Logger log = Logger.getLogger(TestRepository.class);

    public void save() {
        log.info("Test repository save");
    }
}
