package com.zaga.serviceImpl;

import com.zaga.entity.oteltrace.OtelTrace;
import com.zaga.repo.command.TraceCommandRepo;
import com.zaga.service.TraceService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TraceServiceImpl implements TraceService{

    @Inject
    TraceCommandRepo traceRepo;

    @Override
    public void createProduct(OtelTrace trace) {
        traceRepo.persist(trace);
    }
    
}
