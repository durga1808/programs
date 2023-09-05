package com.zaga.repo;

import com.zaga.entity.metrics.MetricMain;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MetricRepo implements PanacheMongoRepository<MetricMain>{
    
}
