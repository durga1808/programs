package com.zaga.repo;

import com.zaga.entity.queryentity.metric.MetricDTO;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
@ApplicationScoped
public class MetricQueryRepo implements PanacheMongoRepository<MetricDTO> {
    
}
