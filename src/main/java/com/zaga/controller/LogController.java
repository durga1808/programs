package com.zaga.controller;

import java.util.List;

import org.bson.Document;

import com.zaga.entity.otellog.OtelLog;
import com.zaga.entity.queryentity.log.LogRecordDTO;
import com.zaga.handler.LogQueryHandler;


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
    LogQueryHandler logQueryHandler;

    

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
    public  List<Document>  aggregateDocuments(@PathParam("serviceName") String serviceName) {
        return logQueryHandler.aggregateDocuments(serviceName);
    }

    @GET
    @Path("/exactdata/{serviceName}")
    public List<LogRecordDTO> getLogData(@PathParam("serviceName") String serviceName) {
        return logQueryHandler.extractLogData(serviceName);
    }
    
    
}

