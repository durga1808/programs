package com.zaga.repo;

import com.zaga.entity.kepler.KeplerMetric;
import com.zaga.entity.queryentity.kepler.KeplerMetricDTO;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class KeplerMetricRepo implements PanacheMongoRepository<KeplerMetricDTO> {
    
}
