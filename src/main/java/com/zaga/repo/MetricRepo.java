package com.zaga.repo;

import com.zaga.entity.otelmetric.OtelMetric;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MetricRepo implements PanacheMongoRepository<OtelMetric>{
    
}
