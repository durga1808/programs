package com.zaga.handler.query;

import java.util.List;

import com.zaga.entity.oteltrace.OtelTrace;
import com.zaga.repo.query.TraceQueryRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TraceQueryHandler {

    @Inject
    TraceQueryRepo traceQueryRepo;

    public List<OtelTrace> getTraceProduct(OtelTrace trace) {
        return traceQueryRepo.listAll();
    }
    
}
