package com.zaga.repo;


import com.zaga.entity.queryentity.cluster_utilization.ClusterUtilizationDTO;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClusterUtilizationDTORepo implements PanacheMongoRepository<ClusterUtilizationDTO> {
     
}
