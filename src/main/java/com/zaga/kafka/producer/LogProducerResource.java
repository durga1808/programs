package com.zaga.kafka.producer;

import com.zaga.entity.otellog.OtelLog;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/log")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LogProducerResource {
    
    @Inject
    private LogProducerService logProducerService;


    @POST
    public Response sendLogDetails(OtelLog otelLog){
        logProducerService.send(otelLog);
        return Response.ok().build();
    }
}
