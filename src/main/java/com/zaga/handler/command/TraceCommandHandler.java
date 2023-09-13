package com.zaga.handler.command;

import com.zaga.entity.oteltrace.OtelTrace;
import com.zaga.repo.command.TraceCommandRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TraceCommandHandler {

    @Inject
    TraceCommandRepo traceCommandRepo;

    public void createTraceProduct(OtelTrace trace) {
        traceCommandRepo.persist(trace);
    }
    
}
