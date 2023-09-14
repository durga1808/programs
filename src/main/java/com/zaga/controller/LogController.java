package com.zaga.controller;

import java.util.List;

import org.bson.Document;

import com.zaga.entity.otellog.OtelLog;
import com.zaga.handler.command.LogCommandHandler;
import com.zaga.handler.query.LogQueryHandler;

import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
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
    public Response createProduct(OtelLog logs) {
        try {
            //System.out.println("----------------");
            logCommandHandler.createLogProduct(logs);
            return Response.status(200).entity(logs).build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }  

    @GET
    @Path("/getByServiceName")
    public List<Document> getLogByServiceName(@QueryParam("serviceName") String serviceName){
        return logQueryHandler.getLogByServiceName(serviceName);
    }

    @GET
    @Path("/bySeverity/{severityText}")
    public List<Document> getLogsBySeverityText(@QueryParam("severityText") String severityText) {
        return logQueryHandler.getLogsBySeverityText(severityText);
    }

    @GET
    @Path("/search/{serviceName}/{severityText}")
    public List<Document> getLogsByServiceNameAndSeverityText(String serviceName, String severityText){
        return logQueryHandler.getLogsByServiceNameAndSeverityText(serviceName, severityText);
    }

    @GET
    @Path("/service/{serviceName}")
    public   List<Document> aggregateDocuments(@PathParam("serviceName") String serviceName) {
        return logQueryHandler.aggregateDocuments(serviceName);
    }
    
}

