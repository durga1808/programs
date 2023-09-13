package com.zaga.controller;

import com.zaga.entity.otellog.OtelLog;
import com.zaga.handler.command.LogCommandHandler;
import com.zaga.handler.query.LogQueryHandler;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/logs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LogController {
    
    @Inject
    LogCommandHandler logCommandHandler;

    @Inject
    LogQueryHandler logQueryHandler;

    @POST
    @Path("/create")
    public Response createProduvct(OtelLog logs) {
        try {
            //System.out.println("----------------");
            logCommandHandler.createLogProduct(logs);
            return Response.status(200).entity(logs).build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }  
}

