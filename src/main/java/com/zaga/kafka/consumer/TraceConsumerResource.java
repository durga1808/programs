package com.zaga.kafka.consumer;

import java.util.List;

import com.zaga.entity.oteltrace.OtelTrace;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@ApplicationScoped

@Path("/kafka-consumer")
public class TraceConsumerResource {

      @Inject
    private TraceConsumerService traceConsumerService; 

    @GET
    public List<OtelTrace> getTraceDetails() {
       return traceConsumerService.getDetails();
    }
    
}


   