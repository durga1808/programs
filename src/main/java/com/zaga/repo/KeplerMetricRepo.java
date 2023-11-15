package com.zaga.repo;

import com.zaga.entity.kepler.KeplerMetric;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class KeplerMetricRepo implements PanacheMongoRepository<KeplerMetric> {
    
}
