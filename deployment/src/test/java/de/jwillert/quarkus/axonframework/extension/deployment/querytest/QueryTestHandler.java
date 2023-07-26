package de.jwillert.quarkus.axonframework.extension.deployment.querytest;

import de.jwillert.quarkus.axonframework.extension.deployment.aggregatetest.TestItem;
import org.axonframework.queryhandling.QueryHandler;

public class QueryTestHandler {

    public QueryTestHandler() {
    }

    @QueryHandler
    public TestItem on(RequestTestItemQuery itemQuery) {
        return new TestItem(itemQuery.getLookupId(), "some text from queryhandler");
    }

}
