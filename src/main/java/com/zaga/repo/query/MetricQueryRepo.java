package com.zaga.repo.query;

import com.zaga.entity.otelmetric.OtelMetric;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
@ApplicationScoped
public class MetricQueryRepo implements PanacheMongoRepository<OtelMetric> {
    
}
