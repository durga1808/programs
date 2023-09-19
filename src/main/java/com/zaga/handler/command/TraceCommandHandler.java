package com.zaga.handler.command;

import java.util.List;

import com.zaga.entity.oteltrace.OtelTrace;
import com.zaga.entity.queryentity.trace.TraceDTO;
import com.zaga.repo.command.TraceCommandRepo;
import com.zaga.repo.query.TraceQueryRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TraceCommandHandler {

    @Inject
    TraceCommandRepo traceCommandRepo;

    @Inject
    TraceQueryRepo traceQueryRepo;

    public void createTraceProduct(OtelTrace trace) {
        traceCommandRepo.persist(trace);

        // Extract the traceId from the OtelTrace object
        String traceId = extractTraceId(trace);

        System.out.println("Trace ID: " + traceId);

        TraceDTO traceDTO = new TraceDTO();
        traceDTO.setTraceId(traceId);

        traceQueryRepo.persist(traceDTO);

    }

    
    public List<OtelTrace> getTraceProduct(OtelTrace trace) {
        return traceCommandRepo.listAll();
    }

    private String extractTraceId(OtelTrace trace) {
        try {
            // Extract the traceId from the OtelTrace object
            String traceId = trace.getResourceSpans().get(0).getScopeSpans().get(0).getSpans().get(0).getTraceId();
            
            return traceId;
            // String traceId = jsonNode.path("traceId").asText();
            // return traceId;
        } catch (Exception e) {
            e.printStackTrace();
            return null; 
        }
    }
}

