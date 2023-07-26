package de.jwillert.quarkus.axonframework.extension.deployment.aggregatetest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkus.arc.Unremovable;

// @Unremovable is needed otherwise build processor will drop it because it's not injected
// by @Inject or @Provider
@Unremovable
@ApplicationScoped
public class TestService {

    @Inject
    TestRepository testRepository;

    public void doSomethingToTestDIInjection() {
        testRepository.save();
    }
}
