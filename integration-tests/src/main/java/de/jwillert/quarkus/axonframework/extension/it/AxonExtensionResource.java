/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package de.jwillert.quarkus.axonframework.extension.it;

import de.jwillert.quarkus.axonframework.extension.it.command.api.command.CreateShopingListCommand;
import de.jwillert.quarkus.axonframework.extension.it.command.api.command.DeleteShopingListCommand;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;

import java.net.URI;
import java.util.UUID;

@Path("/axon-extension")
@ApplicationScoped
public class AxonExtensionResource {
    // add some rest methods here

    @Inject
    CommandGateway commands;

    @Inject
    QueryGateway queries;

    @GET
    public String hello() {
        return "Hello axon-extension";
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response createShoppingCard() {
        UUID id = UUID.randomUUID();
        commands.sendAndWait(new CreateShopingListCommand(id));
        return Response.created(URI.create(id.toString())).entity(id).build();
    }

    @DELETE
    @Path("{id}")
    public void createShoppingCard(@PathParam("id") UUID id) {
        commands.sendAndWait(new DeleteShopingListCommand(id));
    }

}
