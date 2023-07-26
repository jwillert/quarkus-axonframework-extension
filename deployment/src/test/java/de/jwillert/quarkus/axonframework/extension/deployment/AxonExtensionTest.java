package de.jwillert.quarkus.axonframework.extension.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import de.jwillert.quarkus.axonframework.extension.deployment.aggregatetest.AnotherEventHandler;
import de.jwillert.quarkus.axonframework.extension.deployment.aggregatetest.TestItemAggregate;
import de.jwillert.quarkus.axonframework.extension.deployment.aggregatetest.TestRepository;
import de.jwillert.quarkus.axonframework.extension.deployment.aggregatetest.TestService;
import de.jwillert.quarkus.axonframework.extension.deployment.sagatest.TestSaga;
import de.jwillert.quarkus.axonframework.extension.deployment.sagatest.TestSagaAggregate;
import jakarta.inject.Inject;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import de.jwillert.quarkus.axonframework.extension.deployment.querytest.QueryTestHandler;
import io.quarkus.test.QuarkusUnitTest;

@Testcontainers
public class AxonExtensionTest {

    @Container
    public static DockerComposeContainer environment =
            new DockerComposeContainer(new File("src/test/resources/compose-axon.yml"))
                    .withExposedService("axon-server", 8024);

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addAsResource("application.properties")
                    .addClasses(TestItemAggregate.class, AnotherEventHandler.class,
                            TestService.class, TestRepository.class, TestSaga.class, TestSagaAggregate.class,
                            QueryTestHandler.class));

    @Inject
    CommandGateway commandGateway;

    @Inject
    QueryGateway queryGateway;

//    @Test
//    public void testAxonCommandAndEventHandling() {
//        String randomAggregateIdentifier = UUID.randomUUID().toString();
//        CreateTestItemCommand createTestItemCommand = new CreateTestItemCommand(randomAggregateIdentifier, "customer 2", 404.0);
//        String aggregateId = commandGateway.sendAndWait(createTestItemCommand);
//        assertEquals(randomAggregateIdentifier, aggregateId);
//    }
//
//    @Test
//    public void testQueryHandling() {
//        RequestTestItemQuery testQuery = new RequestTestItemQuery(11L);
//        TestItem testItem = queryGateway.query(testQuery, TestItem.class).join();
//        assertEquals(testItem.getId(), testQuery.getLookupId());
//        assertEquals("some text from queryhandler", testItem.getSomeText());
//    }
//
//    @Test
//    public void testSagaHandling() throws InterruptedException {
//        String aggregateIdentifier = UUID.randomUUID().toString();
//        String returnValue = commandGateway.sendAndWait(new StartSagaFlowCommand(aggregateIdentifier));
//        assertEquals(aggregateIdentifier, returnValue);
//        Thread.sleep(2000); // Give saga some time to process
//    }
}
