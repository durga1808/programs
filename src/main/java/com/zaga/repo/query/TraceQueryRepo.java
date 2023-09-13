package com.zaga.repo.query;

import com.zaga.entity.oteltrace.OtelTrace;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
@ApplicationScoped
public class TraceQueryRepo implements PanacheMongoRepository<OtelTrace> {
    
}
