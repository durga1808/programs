package com.zaga.repo.command;

import com.zaga.entity.oteltrace.OtelTrace;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TraceCommandRepo implements PanacheMongoRepository<OtelTrace>{
    
}
