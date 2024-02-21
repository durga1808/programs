package com.zaga.repo;

import com.zaga.entity.clusterutilization.OtelClusterUutilization;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClusterUtilizationRepo implements PanacheMongoRepository<OtelClusterUutilization> {
    
}
